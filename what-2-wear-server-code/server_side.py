import logging
import random
import sys
import models
#import cgi

#from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from django.utils import simplejson
#from google.appengine.ext.webapp import template
from google.appengine.api import images
from google.appengine.ext import db 



logging.getLogger().setLevel(logging.DEBUG)

class MainPage(webapp.RequestHandler):
    
    def get(self):
#        """this method is for testing purpose only and will be deleted!!!"""
#        self.response.out.write('<html><body>')
#        """display a html form"""
#        values = {"try1": [1]}
#        self.response.out.write(template.render('main.html', values))
#        self.response.out.write("""</body></html>""")
       
        self.response.headers['Content-Type'] = 'text/html'
        self.response.out.write("""<html><body>Welcome!<br>""")

        self.response.out.write("""in order to load data please press the load button:<br>
                                   <div class="mybutton">    
                                   <button onclick="window.location='/loadImages'" 
                                   style="width:80;height:24">load</button><br> 
                                   </div>""")
        self.response.out.write("""in order to clear all data please press the clear button:<br>
                                   <div class="mybutton">    
                                   <button onclick="window.location='/clearImages'" 
                                   style="width:80;height:24">clear</button><br> 
                                   </div>
                                   </body></html>""")



class SearchInDataStore(webapp.RequestHandler):   
    def post(self):
        """ this method searches for images in the data store according to certain characteristics and items
        this method receives a get request with keys: 
            gender_id 
            items_num_id
            item1_type_id
            item1_color_id
            ... (more items with same structure, different index)
            style_id (optional)
            season_id (optional)           
            """

        """get the search parameters from the request"""
        gender = self.request.get("gender_id")
        items_num = int(self.request.get("items_num_id"))
        style = self.request.get("style_id")
        season = self.request.get("season_id")
        
        if (gender is None) or (items_num is None):
            self.error(404) #not found
            self.response.out.write('One of the request keys was not found')
            return
        
        types_list = []
        colors_list = []
        """get all items' types and colors in two lists"""
        for i in range(items_num):
            type = self.request.get("item"+str(i+1)+"_type_id")
            color = self.request.get("item"+str(i+1)+"_color_id")
            if (type is None) or (color is None):
                self.error(404) #not found
                self.response.out.write('One of the request keys was not found')
                return                
            types_list.append(str(type))
            colors_list.append(str(color))

        """create the list that will be returned from the request (in json format)"""
        imageList = []
        
        """prepare a query"""
        query = models.ImageStruct.all() 
        query.filter("subject_gender = ", gender).order("-avg_image_rating") 
        
        if style:
            query.filter("style = ", style) 
        if season:
            query.filter("season = ", season) 

        results = query.fetch(100)
        if results:
            """filter according to items"""
            for cur_image in results:
                image_items = cur_image.itemstruct_set
                isMissing = 0
                """go over all the items the request sent, and check if every item appears in the certain image"""
                """ check if a certain image has all the items the request sent 
                (for each item in the items list- check if the image contains it) """
                for i in range(items_num):
                    q = image_items.filter("item_type = ", types_list[i]).filter("item_color = ", colors_list[i])
                    if q.get() is None:
                        isMissing = 1
                        break 
                if isMissing == 0:
                    imageList.append(cur_image.to_dict())
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(imageList))                    

               
class UpdateRating:
    def post(self):
        """updates the rating summary, raters counter and average rating.
        this method receives a get request with keys: key_id = an id of an image struct 
                                                      rating_id = a rating we want to update the current rating with
        sends new rating to caller"""
        key = self.request.get("key_id")
        rating = self.request.get("rating_id")
        if (key is None) or (rating is None):
            self.error(404) #not found
            self.response.out.write('One of the request keys was not found')
            return              
        image_struct = db.get(key)
        rating = float(rating)
        image_struct.rating_sum = image_struct.rating_sum + rating
        image_struct.rating_num = image_struct.rating_num + 1 
        image_struct.avg_image_rating = image_struct.rating_sum/image_struct.rating_num
        image_struct.put()
        
        """send the updated rating to the caller"""
        self.response.headers['Content-Type'] = "application/json" 
        return_str= [{"rating_id" : image_struct.avg_image_rating}]
        self.response.out.write(simplejson.dumps(return_str))

class GetImageByKeyID (webapp.RequestHandler):
    def get(self):
        """this method receives an image id key and return the url address of the suitable image"""
        key = self.request.get("key_id")
        if key is None:
            self.error(404) #not found
            self.response.out.write('One of the request keys was not found')
            return  
        
        image_struct = db.get(key)
        if image_struct.image:
            self.response.headers['Content-Type'] = "image/jpg"
            self.response.out.write(image_struct.image)
        else:
            self.redirect('/my_images/no_image.jpg')


class AddImagesToDataStore(webapp.RequestHandler):
    def post(self):

        gender = self.request.get("gender_id")
        style = str(self.request.get("style_id"))
        season = str(self.request.get("season_id"))
        items_number = self.request.get("items_num_id")
        file_image = self.request.POST.get("img_id")
        
        if (gender is None) or (items_number is None) or (file_image is None):
            self.error(404) #not found
            self.response.out.write('One of the request keys was not found')
            return
        
        image_struct = models.ImageStruct()
        
        image_struct.subject_gender = gender
        image_struct.items_num = int(items_number)
          
        if style: 
            image_struct.style = style
        if season:
            image_struct.season = season
        
        img_data = file_image.file.read()
        try:
            img = images.Image(img_data)
            img.resize(190, 190)
            image_struct.image = img.execute_transforms(images.JPEG)
            image_struct.assign_random()
            image_struct.put()
            for i in range(image_struct.items_num):
                item = models.ItemStruct(image_struct = image_struct) 
                """fill-in  the item properties"""
                type = 'item' + str(i + 1) + '_type_id'
                color = 'item' + str(i + 1) + ' _color_id'
                item.item_type = self.request.get(type)     
                item.item_color = self.request.get(color)
                if (item.item_type is None) or (item.item_color is None):
                    #TODO: delete image and previous items
                    self.error(404) #not found
                    self.response.out.write('One of the request keys was not found')
                    return 
                item.put()
            self.response.set_status(200)
            self.response.out.write('success')
        except images.BadImageError:
            self.error(400)
            self.response.out.write('A problem occurred during processing the image.')
        except images.NotImageError:
            self.error(400)
            self.response.out.write('Invalid image format.'
                                    'Use JPEG, GIF, PNG, BMP, TIFF, and ICO files only.')
        except images.LargeImageError:
            self.error(400)
            self.response.out.write('The image provided was too large to process.')        

class FeelingLucky(webapp.RequestHandler):
    def get(self):        
        gender = self.request.get("gender_id")
        if (gender is None):
            self.error(404) #not found
            self.response.out.write('One of the request keys was not found')
            return
        
        """get a random image"""
        #query = db.Query(models.ImageStruct)
        #query.filter('subject_gender =', gender)
        #query.filter('random_num >', rand).order('-random_num')
        
        isFound = 0
        result = None
        while (isFound == 0):
            """get a random number"""
            rand = random.randint(1, sys.maxint)
            query = db.GqlQuery("SELECT * FROM ImageStruct WHERE subject_gender = :1 " +
                                "AND random_num > :2 " +
                                "ORDER BY random_num ASC ", gender, rand)
            result = query.get()
            if not (result is None):
                isFound = 1;

        """create the list that will be returned from the request (in json format)"""
        imageList = []
        imageList.append(result.to_dict())
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(imageList))   

application = webapp.WSGIApplication([
    ('/', MainPage),
    ('/img', GetImageByKeyID),
    ('/search', SearchInDataStore),
    ('/upload', AddImagesToDataStore),
    ('/update-rating', UpdateRating),
    ('/im-feeling-lucky', FeelingLucky)
], debug=True)

def main():
    random.seed()
    run_wsgi_app(application)
    
if __name__ == '__main__':
    main()