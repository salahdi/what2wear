import logging
import random
import sys
import models
import os
os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'

from google.appengine.dist import use_library
use_library('django', '1.2')

from django.utils import simplejson

from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.api import images
from google.appengine.api import users
from google.appengine.ext import db 
from google.appengine.ext.webapp import template


logging.getLogger().setLevel(logging.DEBUG)

class MainPage(webapp.RequestHandler):    
    def get(self):

        user = users.GetCurrentUser()
        login = users.CreateLoginURL(self.request.uri)
        logout = users.CreateLogoutURL(self.request.uri)

        template_file_name = 'mainpage.html'
        template_values = {'login': login, 'logout': logout, 'user': user}

        path = os.path.join(os.path.dirname(__file__), template_file_name)
        self.response.out.write(template.render(path, template_values))


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
            season_id (optional)"""

        """get the search parameters from the request"""
        gender = self.request.get("gender_id")
        items_num = int(self.request.get("items_num_id"))
        styles = self.request.get("style_id", allow_multiple = True)
        seasons = self.request.get("season_id", allow_multiple = True)

        if (not gender) or (not items_num):
            self.response.set_status(404)
            self.response.out.write('One of the requested keys was not found')
            return
        
        """prepare a query"""
        queryStr = "SELECT * FROM ImageMetadataStruct WHERE subject_gender = '"+ gender + "'" 

        if styles != [u'']:
            temp = ""
            for style in styles:
                if temp == "":
                    temp += "'" + unicode(style)+ "'"
                else:
                    temp += ", '" + unicode(style)+ "'"
            queryStr +=" AND style IN (" + temp + ")"
        if seasons != [u'']:
            temp = ""
            for season in seasons:
                if temp == "":
                    temp += "'" + unicode(season)+ "'"
                else:
                    temp += ", '" + unicode(season)+ "'"
            queryStr +=" AND season IN (" + temp + ")"
        
        for i in range(items_num):
            type = self.request.get("item"+str(i+1)+"_type_id")
            color = self.request.get("item"+str(i+1)+"_color_id")
            if (not type) or (not color):
                self.response.set_status(404)
                self.response.out.write('One of the requested keys was not found')
                return
            queryStr += " AND items_list = '" + unicode(type+","+color) + "'"
        
        queryStr += " ORDER BY avg_image_rating DESC LIMIT 100"

        logging.info(queryStr)

        query = db.GqlQuery(queryStr)

        """create the list that will be returned from the request (in json format)"""
        imageList = []

        if query.get():
            for cur_image in query:
                imageList.append(cur_image.to_dict())
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(imageList))                    


"""a method to update an image rating and the rating of the user who uploaded it"""
def update_rating_image(image_struct, rating):
    rating = float(rating)
    old_rating = image_struct.avg_image_rating
    image_struct.rating_sum = image_struct.rating_sum + rating
    image_struct.rating_num = image_struct.rating_num + 1 
    image_struct.avg_image_rating = image_struct.rating_sum/image_struct.rating_num
    image_struct.put()

    return old_rating


"""a method to update an image rating and the rating of the user who uploaded it"""
def update_rating_user(user, old_rating, avg_image_rating):
    user.update_score(old_rating, avg_image_rating)

               
class UpdateRating(webapp.RequestHandler):
    
    def get(self):
        """updates the rating summary, raters counter and average rating.
        this method receives a get request with keys: key_id = an id of an image meta data struct 
                                                      rating_id = a rating we want to update the current rating with
        sends new rating to caller"""
        key = self.request.get("key_id")
        rating = self.request.get("rating_id")
        if (not key) or (not rating):
            self.response.set_status(404)
            self.response.out.write('One of the requested keys was not found')
            return  
            
        image_struct = db.get(key)
        if (not image_struct):
            self.response.set_status(404)
            self.response.out.write('The image was not found')
            return   
   
        old_rating = db.run_in_transaction(update_rating_image, image_struct, rating)

        db.run_in_transaction(update_rating_user, image_struct.user, old_rating, image_struct.avg_image_rating)        
        
        """send the updated rating to the caller"""
        avg = "%.2f" % image_struct.avg_image_rating
        return_str = [{"rating_id" : avg}]
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(return_str))

class GetImageByKeyID (webapp.RequestHandler):
    def get(self):
        """this method receives an image id key and return the url address of the suitable image"""
        key = self.request.get("image_key_id")
        if not key:
            self.response.set_status(404)
            self.response.out.write('One of the requested keys was not found')
            return  
        
        image_struct = db.get(key)
        
        if (not image_struct):
            self.response.set_status(404)
            self.response.out.write('An image with the requested key was not found')
            return
        
        if image_struct.image:
            self.response.headers['Content-Type'] = "image/jpg"
            self.response.out.write(image_struct.image)
        else:
            self.redirect('/my_images/no_image.jpg')


class UploadImagesToDataStore(webapp.RequestHandler):
    
    def post(self):
        """this method receives a post request with keys: 
            gender_id 
            items_num_id
            item1_type_id
            item1_color_id
            ... (more items with same structure, different index)
            style_id (optional. can receive several styles)
            season_id (optional. can receive several seasons)   
            email_or_id_id
            account_type_id
            and also an image file with key img_id.
            The method adds the image and image details as ImageMetadataStruct and ImageStruct
            to the data store."""        
        gender = self.request.get("gender_id")
        styles = self.request.get("style_id", allow_multiple = True)
        seasons = self.request.get("season_id", allow_multiple = True)
        items_number = self.request.get("items_num_id")
        file_image = self.request.POST.get("img_id")
        email_or_id = self.request.get("email_or_id_id")
        account_type = self.request.get("account_type_id")
        
        if (not gender) or (not items_number) or (not email_or_id) or (not account_type):
            self.response.set_status(404)
            logging.info('One of the requested keys was not found');
            self.response.out.write('One of the requested keys was not found')
            return
        
        """create a new image metadata"""
        imageMetadata = models.ImageMetadataStruct()
        
        imageMetadata.subject_gender = gender
        imageMetadata.items_num = int(items_number)
        if (styles and (styles != [u''])):
            imageMetadata.style = styles
        else:
            imageMetadata.style = []
        if (seasons and (seasons != [u''])):
            imageMetadata.season = seasons
        else:
            imageMetadata.season = []
        img_data = file_image.file.read()
        try:
            img = images.Image(img_data)
            img.resize(300, 300)
            imageStruct = models.ImageStruct(image = img.execute_transforms(images.JPEG))
            imageStruct.put()
            imageMetadata.image = imageStruct
            for i in range(imageMetadata.items_num):
                """fill-in  the item properties"""
                type = self.request.get("item"+str(i+1)+"_type_id")
                color = self.request.get("item"+str(i+1)+"_color_id")
                if (not type) or (not color):
                    self.response.set_status(404)
                    logging.info('One of the requested keys (type or color) was not found')
                    self.response.out.write('One of the requested keys was not found')
                    return 
                (imageMetadata.items_list).append(type+","+color)
            imageMetadata.put()
            imageMetadata.assign_user(email_or_id)
            imageMetadata.put()

            self.response.headers['Content-Type'] = "application/json"
            self.response.out.write(simplejson.dumps(imageMetadata.to_dict()))
            return

        except images.BadImageError:
            self.response.set_status(400)
            self.response.out.write('A problem occurred during processing the image.')
        except images.NotImageError:
            self.response.set_status(400)
            self.response.out.write('Invalid image format.'
                                    'Use JPEG, GIF, PNG, BMP, TIFF, and ICO files only.')
        except images.LargeImageError:
            self.response.set_status(400)
            self.response.out.write('The image provided was too large to process.')    
        except db.TransactionFailedError:
            self.response.set_status(500)
            self.response.out.write('Uploading image failed')   
        except db.TransactionFailedError:
            self.response.set_status(500)
            self.response.out.write('adding the image to the data base failed')  
                                   

class FeelingLucky(webapp.RequestHandler):
    
    def get(self):
        """ This method receives a gender with key gender_id (optional) and returns a random image
        from the data store. If the method received a gender, the random image will be an image
        of a subject with this gender"""        
        
        gender = self.request.get("gender_id", default_value = "")
        queryStr = ""
        if (gender in ["female", "male"]):
            queryStr = "SELECT * FROM ImageMetadataStruct WHERE subject_gender = '"+gender+"' AND random_num > :1"
        else:
            queryStr = "SELECT * FROM ImageMetadataStruct WHERE random_num > :1"
        queryStr += " ORDER BY random_num ASC"
        
        """get a random image"""       
        isFound = 0
        result = None
        while (isFound == 0):
            """get a random number"""
            rand = random.randint(1, sys.maxint)
            query = db.GqlQuery(queryStr, rand)
            result = query.get()
            if result:
                isFound = 1;

        """create the list that will be returned from the request (in json format)"""
        imageList = []
        imageList.append(result.to_dict())
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(imageList))   

class TopFive(webapp.RequestHandler):

    """ The method receives account type (optional).
        this method returns a list of 5 users, each user holds details of:
        user_nickname_id - the user's nickname
        score_id - the user's score (sum of average picture score)
        images_num_id - number of images this user uploaded"""
    def get(self):
        account_type = self.request.get("account_type_id", default_value = "")
        
        if (account_type in ["google", "facebook"]):
            query = db.GqlQuery("SELECT * FROM UserStruct WHERE account_type = :1 ORDER BY score DESC", account_type)
        else:
            query = db.GqlQuery("SELECT * FROM UserStruct ORDER BY score DESC")
       
        results = query.fetch(5)
        usersList = []
        for result in results:
            usersList.append(result.to_dict())
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(usersList))   
        
class SignUser(webapp.RequestHandler):
    
    def get(self):
        """ This method receives an email or id (with key email_or_id_id), an account type
        (with key account_type_id) and a name (with key_name_id).
        The method checks if this user (with unique identifier email_or_id_id) already has a
        matching UserStruct in the data store.
        If yes- returns a json response "registered".
        If no- adds a UserStruct with the user details and returns "new"
        If one of the key is missing returns "error" """        
        
        email_or_id = self.request.get("email_or_id_id")
        account_type = self.request.get("account_type_id")
        name = self.request.get("name_id")
        if (not email_or_id) or (not account_type) or ((not name) and (unicode(account_type) == "facebook")):
            self.response.set_status(404)
            self.response.out.write('One of the requested keys was not found')
            self.response.headers['Content-Type'] = "application/json"
            self.response.out.write(simplejson.dumps({"user_status": "error"})) 
        else:
            """ check if this user exists in the datastore"""
            user = models.get_user_by_email_or_id(unicode(email_or_id))
            if not user:
                """ add the user to the datastore"""
                new_user = models.UserStruct(email_or_id = email_or_id, account_type = account_type)
                if (account_type == "facebook"):
                    new_user.name = unicode(name).replace('_', ' ')
                else:
                    user = users.User(unicode(email_or_id))
                    new_user.name = user.nickname()
                new_user.put()
                self.response.headers['Content-Type'] = "application/json"
                self.response.out.write(simplejson.dumps({"user_status": "new"}))
            else:
                self.response.headers['Content-Type'] = "application/json"
                self.response.out.write(simplejson.dumps({"user_status": "registered"}))

class UserImages(webapp.RequestHandler):
       
    def get(self):
        """ thTis method returns all the images associated with a certain user according to his email address
        or id number.
        This method receives a get request with keys email_or_id_id, sort_id, num_id
        and offset_id (the last 3 are optional)"""

        email_or_id = self.request.get("email_or_id_id")
        sort_param = self.request.get("sort_id", default_value = "rating")
        num = self.request.get("num_id", default_value = "100")
        offset = self.request.get("offset_id", default_value = "0")

        if not email_or_id :
            self.response.set_status(404)
            self.response.out.write('One of the requested keys was not found')
            return
        
        user = models.get_user_by_email_or_id(email_or_id)
        
        if not user:
            self.response.set_status(404)
            self.response.out.write('An user with the requested email or id was not found')
            return
        
        """create the list that will be returned from the request (in json format)"""
        imageList = []
        
        user_images = user.imagemetadatastruct_set
        
        if (sort_param == "rating"):
            user_images = user_images.order('-avg_image_rating')
        elif (sort_param == "date"):
            user_images = user_images.order('-date')
        else:
            self.response.set_status(404)
            self.response.out.write("sort_id key must be rating or date") 

        user_images = user_images.fetch(int(num), offset=int(offset)) 

        for image in user_images:
            imageList.append(image.to_dict())
        
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(imageList))                    

class IntersectLists(webapp.RequestHandler):
      
    def post(self):
        """this method receives a list of users emails or ids and an account type.
        The method returns a list of users that are registered to the application (has a suitable
        UserStruct in the data store) and appear on the given list""" 
    
        friends_list = self.request.get("email_or_id_id", allow_multiple = True)
        account_type = self.request.get("account_type_id")
        
        friendsList = []

        for friend in friends_list:
            user = models.UserStruct.gql("WHERE email_or_id = :1 AND account_type = :2", friend, account_type).get()
            if user:
                friendsList.append(user.to_dict())
        
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(friendsList)) 


class NewImages(webapp.RequestHandler): 

    def get(self):
        """this method receives a number of images, an offset number and a gender (last 2 are optional).
       The method returns the x newest images according the the number and offset in the input.
       If the method received a certain gender, the results will include images of the certain gender only"""        
        
        num = self.request.get("number_id")
        offsetNum = self.request.get("offset_id", default_value = "0")
        gender = self.request.get("gender_id", default_value = "")

        if (not num) or (not offsetNum):
            self.response.out.write('One of the requested keys was not found')
            self.response.set_status(404)
            return
        
        query = models.ImageMetadataStruct.all()
        
        if (gender in ["female", "male"]):
            query.filter("subject_gender =", gender)
        
        query.order("-date")
        
        results = query.fetch(int(num), int(offsetNum))

        images = []

        for result in results:
            images.append(result.to_dict())
        
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(images))

application = webapp.WSGIApplication([
    ('/', MainPage),
    ('/img', GetImageByKeyID),
    ('/search', SearchInDataStore),
    ('/upload', UploadImagesToDataStore),
    ('/update-rating', UpdateRating),
    ('/im-feeling-lucky', FeelingLucky),
    ('/top-five', TopFive),
    ('/sign-user', SignUser),
    ('/user-images', UserImages),
    ('/intersect-lists', IntersectLists),
    ('/new-images', NewImages)
], debug=True)

def main():
    random.seed()       
    run_wsgi_app(application)
    
if __name__ == '__main__':
    main()