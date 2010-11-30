import logging

#import cgi
#import sys #import sys module for printing to stdout


from google.appengine.ext import db
#from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from django.utils import simplejson
from google.appengine.ext.webapp import template
import models


logging.getLogger().setLevel(logging.DEBUG)

class MainPage(webapp.RequestHandler):
    
    def get(self):
        
        
        """this method is for testing purpose only and will be deleted!!!"""
        self.response.out.write('<html><body>')
        """display a html form"""
        values = {"try1": [1]}
        self.response.out.write(template.render('main.html', values))
        self.response.out.write("""</body></html>""")
        
        
        
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
        style = str(self.request.get("style_id"))
        season = str(self.request.get("season_id"))
        types_list = []
        colors_list = []
        """get all items' types and colors in two lists"""
        for i in range(items_num):
            types_list.append(str(self.request.get("item"+str(i+1)+"_type_id")))
            colors_list.append(str(self.request.get("item"+str(i+1)+"_color_id")))
    
        """create the list that will be returned from the request (in json format)"""
        imageList = []

        """get data from the data store, filter it according to gender and sort in ascending order.
        By default will fetch up to 2000 results"""
        all_images = models.ImageStruct.gql("WHERE subject_gender = :1 ORDER BY avg_image_rating ASC", gender)
        #all_images = db.GqlQuery("SELECT * FROM ImageStruct WHERE subject_gender = :1 " +
        #                         "ORDER BY avg_image_rating ASC", gender)
        if all_images:
            """filter according to style, if needed"""
            if style != '':
                all_images.filter("style =", style)
            if all_images:
                """filter according to season, if needed"""
                if season != '':
                    all_images.filter("season =", season)
                if all_images:
                    """filter according to items"""
                    for cur_image in all_images:
                        image_items = cur_image.itemstruct_set
                        suitable = 0;
                        """go over all the items the request send, and check if every item appears in the certain image"""
                        for i in range(items_num):
                            """go over all the items an image contains and check if one of them is suits the current 
                            item from the request items list. if we found a suitable item, we increase the suitable
                            counter and stop searching (break)"""
                            for item in image_items:
                                if ((item.item_type == types_list[i]) and (item.item_color == colors_list[i])):
                                    suitable = suitable + 1
                                    break;  
                            if suitable == items_num:
                                imageList.append(cur_image.to_dict())
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(imageList))        
#        else:
        #    self.error(404)
        #    self.response.out.write('No such player')
               
class UpdateRating:
    def post(self):
        """updates the rating summary, raters counter and average rating.
        this method receives a get request with keys: key_id = an id of an image struct 
                                                      rating_id = a rating we want to update the current rating with
        sends new rating to caller"""
        image_struct = db.get(self.request.get("key_id"))
        rating = float(self.request.get("rating_id"))
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
        image_struct = db.get(self.request.get("key_id"))
        if image_struct and image_struct.image:
            self.response.headers['Content-Type'] = "image/jpg"
            self.response.out.write(image_struct.image)
        else:
            self.redirect('/my_images/no_image.jpg')


application = webapp.WSGIApplication([
    ('/', MainPage),
    ('/img', GetImageByKeyID),
    ('/search', SearchInDataStore),
    ('/update-rating', UpdateRating)
], debug=True)

def main():       
    run_wsgi_app(application)
    
if __name__ == '__main__':
    main()