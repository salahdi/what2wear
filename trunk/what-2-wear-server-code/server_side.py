import logging
import random
import sys
import models

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from django.utils import simplejson
from google.appengine.api import images
from google.appengine.ext import db 


logging.getLogger().setLevel(logging.DEBUG)

class AdminEntrance(webapp.RequestHandler):  
    def get(self):
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
        this method receives a post request with keys: 
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
        
        if (not gender) or (not items_num):
            self.error(404) #not found
            self.response.out.write('One of the requested keys was not found')
            return

        """create the list that will be returned from the request (in json format)"""
        imageList = []
        
        """prepare a query"""
        query = models.ImageStruct.all() 
        query.filter("subject_gender = ", gender).order("-avg_image_rating") 
        
        if style:
            query.filter("style = ", style) 
        if season:
            query.filter("season = ", season) 

        for i in range(items_num):
            type = self.request.get("item"+str(i+1)+"_type_id")
            color = self.request.get("item"+str(i+1)+"_color_id")
            if (not type) or (not color):
                self.error(404) #not found
                self.response.out.write('One of the requested keys was not found')
                return
            query.filter("items_list = ", unicode(type+","+color))
    
        results = query.fetch(100)
        if results:
            for cur_image in results:
                imageList.append(cur_image.to_dict())
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(imageList))                    

               
class UpdateRating(webapp.RequestHandler):
    def get(self):
        """updates the rating summary, raters counter and average rating.
        this method receives a get request with keys: key_id = an id of an image struct 
                                                      rating_id = a rating we want to update the current rating with
        sends new rating to caller"""
        key = self.request.get("key_id")
        rating = self.request.get("rating_id")
        if (not key) or (not rating):
            self.error(404) #not found
            self.response.out.write('One of the requested keys was not found')
            return              
        
        image_struct = db.get(key)
        if (not image_struct):
            self.error(404) #not found
            return 
        
        rating = float(rating)
        old_rating = image_struct.avg_image_rating
        image_struct.rating_sum = image_struct.rating_sum + rating
        image_struct.rating_num = image_struct.rating_num + 1 
        image_struct.avg_image_rating = image_struct.rating_sum/image_struct.rating_num
        image_struct.put()
        
        """update the rating of the user who uploaded this image"""
        user = image_struct.user
        user.update_score(old_rating, image_struct.avg_image_rating)
        
        """send the updated rating to the caller"""
        self.response.headers['Content-Type'] = "application/json" 
        return_str = [{"rating_id" : image_struct.avg_image_rating}]
        self.response.out.write(simplejson.dumps(return_str))

class GetImageByKeyID (webapp.RequestHandler):
    def get(self):
        """this method receives an image id key and return the url address of the suitable image"""
        key = self.request.get("key_id")
        if not key:
            self.error(404) #not found
            self.response.out.write('One of the requested keys was not found')
            return  
        
        image_struct = db.get(key)
        
        if (not image_struct):
            self.error(404)
            self.response.out.write('An image with the requested key was not found')
            return
        
        if image_struct.image:
            self.response.headers['Content-Type'] = "image/jpg"
            self.response.out.write(image_struct.image)
        else:
            self.redirect('/my_images/no_image.jpg')


class AddImagesToDataStore(webapp.RequestHandler):
    def post(self):
        gender = self.request.get("gender_id")
        style = self.request.get("style_id")
        season = self.request.get("season_id")
        items_number = self.request.get("items_num_id")
        file_image = self.request.POST.get("img_id")
        user_email = self.request.get("email_id")
        
        if (not gender) or (not items_number) or (not user_email):
            self.error(404) #not found
            self.response.out.write('One of the requested keys was not found')
            return
        
        """create a new image struct"""
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
            for i in range(image_struct.items_num):
                #item = models.ItemStruct(image_struct = image_struct) 
                """fill-in  the item properties"""
            	type = self.request.get("item"+str(i+1)+"_type_id")
            	color = self.request.get("item"+str(i+1)+"_color_id")
                if (not type) or (not color):
                    self.error(404) #not found
                    self.response.out.write('One of the requested keys was not found')
                    return 
                (image_struct.items_list).append(type+","+color)
            image_struct.put()
            image_struct.assign_user(user_email)
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
        if not gender:
            self.error(404) #not found
            self.response.out.write('One of the requested keys was not found')
            return
        
        """get a random image"""       
        isFound = 0
        result = None
        while (isFound == 0):
            """get a random number"""
            rand = random.randint(1, sys.maxint)
            query = db.GqlQuery("SELECT * FROM ImageStruct WHERE subject_gender = :1 " +
                                "AND random_num > :2 " +
                                "ORDER BY random_num ASC ", gender, rand)
            result = query.get()
            if result:
                isFound = 1;

        """create the list that will be returned from the request (in json format)"""
        imageList = []
        imageList.append(result.to_dict())
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(imageList))   

class TopFive(webapp.RequestHandler):
    """ this method return a list of 5 users, each user holds details of:
        user_nickname_id - the user's nickname
        score_id - the user's score (sum of average picture score)
        images_num_id - number of images this user uploaded"""
    def get(self):
        query = db.GqlQuery("SELECT * FROM UserStruct ORDER BY score DESC")
        results = query.fetch(5)
        usersList = []
        for result in results:
            usersList.append(result.to_dict())
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(usersList))   
        
class SignUser(webapp.RequestHandler):
    def get(self):
        user_email = self.request.get("email_id")
        #gender = self.request.get("gender_id")
        #gender = self.request.get("location_id")
        user = users.User(user_email)
        if not user:
            self.error(404)
            self.response.out.write('One of the requested keys was not found')
        else:
            """ check if this user exists in the datastore"""
            query = db.GqlQuery("SELECT * FROM UserStruct WHERE user = :1", user)
            user_struct = query.get()
            if not user_struct:
                new_user = models.UserStruct(user = user)
                new_user.put()

class UserImages(webapp.RequestHandler):   
    def get(self):
        """ this method returns all the images associated with a certain user according to his email address
        this method receives a get request with key email_id"""

        user_email = self.request.get("email_id")
        if not user_email:
            self.error(404)
            self.response.out.write('One of the requested keys was not found')
            return
        
        user = models.get_user_by_email(user_email)
        
        if not user:
            self.error(404)
            self.response.out.write('An user with the requested mail was not found')
            return
        
        """create the list that will be returned from the request (in json format)"""
        imageList = []
        
        user_images = user.imagestruct_set.order('-avg_image_rating').fetch(100)

        for image in user_images:
            imageList.append(image.to_dict())
        
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(imageList))                    


application = webapp.WSGIApplication([
    ('/admin-entrance', AdminEntrance),
    ('/img', GetImageByKeyID),
    ('/search', SearchInDataStore),
    ('/upload', AddImagesToDataStore),
    ('/update-rating', UpdateRating),
    ('/im-feeling-lucky', FeelingLucky),
    ('/top-five', TopFive),
    ('/sign-user', SignUser),
    ('/user-images', UserImages)
], debug=True)

def main():
    random.seed()       
    run_wsgi_app(application)
    
if __name__ == '__main__':
    main()