import logging
import random
import sys
import models

from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from django.utils import simplejson
from google.appengine.api import images
from google.appengine.api import users
from google.appengine.ext import db 


logging.getLogger().setLevel(logging.DEBUG)


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


class AdminEntrance(webapp.RequestHandler):  
    def get(self):
        self.response.headers['Content-Type'] = 'text/html'
        self.response.out.write("""<html><body>Welcome!<br>""")

        self.response.out.write("""in order to load data please press the load button:<br>
                                   <div class="mybutton">    
                                   <button onclick="window.location='/loadData'" 
                                   style="width:80;height:24">load</button><br> 
                                   </div>""")
        self.response.out.write("""in order to clear all data please press the clear button:<br>
                                   <div class="mybutton">    
                                   <button onclick="window.location='/clearAllData'" 
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
        styles = self.request.get("style_id", allow_multiple = True)
        seasons = self.request.get("season_id", allow_multiple = True)

        if (not gender) or (not items_num):
            self.response.set_status(404)
            self.response.out.write('One of the requested keys was not found')
            return

        """create the list that will be returned from the request (in json format)"""
        imageList = []
        
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

        query = db.GqlQuery(queryStr)

        if query.get():
            for cur_image in query:
                imageList.append(cur_image.to_dict())
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(imageList))                    

               
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


class AddImagesToDataStore(webapp.RequestHandler):
    def post(self):
        gender = self.request.get("gender_id")
        styles = self.request.get("style_id", allow_multiple = True)
        seasons = self.request.get("season_id", allow_multiple = True)
        items_number = self.request.get("items_num_id")
        file_image = self.request.POST.get("img_id")
        email_or_id = self.request.get("email_or_id_id")
        account_type = self.request.get("account_type_id")
        
        if (not gender) or (not items_number) or (not email_or_id) or (not account_type):
            self.response.set_status(404)
            self.response.out.write('One of the requested keys was not found')
            self.response.headers['Content-Type'] = "application/json"
            self.response.out.write(simplejson.dumps({}))
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
                    self.response.out.write('One of the requested keys was not found')
                    return 
                (imageMetadata.items_list).append(type+","+color)
            imageMetadata.put() #check if necessary!!!
            imageMetadata.assign_user(email_or_id)
            imageMetadata.put()

            self.response.headers['Content-Type'] = "application/json"
            self.response.out.write(simplejson.dumps(imageMetadata.to_dict()))

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

    """ this method return a list of 5 users, each user holds details of:
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
        """ this method returns all the images associated with a certain user according to his email address
        this method receives a get request with key email_id and with key sort_id"""

        email_or_id = self.request.get("email_or_id_id")
        sort_param = self.request.get("sort_id", default_value = "rating")

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
            self.response.headers['Content-Type'] = "application/json"
            self.response.out.write(simplejson.dumps({}))  
              
        user_images.fetch(100) 

        for image in user_images:
            imageList.append(image.to_dict())
        
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(imageList))                    

class intersectLists(webapp.RequestHandler):  
    """this method receives a list of friends nicknames and intersect this list with the users of
    the application""" 
    def post(self):
        """get a list of all friends"""
        friends_list = self.request.get("email_or_id_id", allow_multiple = True)
        account_type = self.request.get("account_type_id")
        
        friendsList = []

        for friend in friends_list:
            user = models.UserStruct.gql("WHERE email_or_id = :1 AND account_type = :2", friend, account_type).get()
            if user:
                friendsList.append(user.to_dict())
        
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(friendsList)) 


class newImages(webapp.RequestHandler): 

    """this method receives a number of images, an offset number and a gender (optional).
       The method returns the x newest images according the the number and offset in the input.
       If the method received a certain gender, the results will include images of the certain gender only"""
    def get(self):
        num = self.request.get("number_id")
        offsetNum = self.request.get("offset_id", default_value = "0")
        gender = self.request.get("gender_id", default_value = "none")

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

class ProperDelete(webapp.RequestHandler):

    """this method receives a key of an entity and the model of the entity and delete it and all related info"""
    def get(self):
        key = self.request.get("key_id")
        model = self.request.get("model_id")

        if not key:
            self.response.set_status(404)
            self.response.out.write('One of the requested keys was not found')
            return  
        
        entity = db.get(key)
    
        if (model == "ImageMetadataStruct"):
            """delete from the user who uploaded the image all related data"""
            user = entity.user
            user.score -= entity.avg_image_rating
            user.images_num -= 1
            """look for users to has this key in their favorites list and delete it from this list"""
            query = models.UserStruct.all()
            query.filter("favorites = ", entity.key())
            offset = 0
            results = query.fetch(100, offset)
            counter = 0
            usersList = []
            while (counter != -1):
                for result in results:
                    index = (result.favorites).index(entity.key())
                    (result.favorites).pop(index)
                    usersList.append(result)
                    counter += 1
                db.put(usersList)
                usersList = []
                if (counter == 100):
                    offset += 100
                    results = query.fetch(100, offset)
                    counter = 0
                else:
                    counter = -1
            user.put()    
            db.delete(entity)
        elif (model == "UserStruct"):
            """delete all images the user uploaded"""
            allImages = entity.imagemetadatastruct_set
            db.delete(allImages)    
            """delete the user from the data base"""
            db.delete(entity)


application = webapp.WSGIApplication([
    ('/admin-entrance', AdminEntrance),
    ('/img', GetImageByKeyID),
    ('/search', SearchInDataStore),
    ('/upload', AddImagesToDataStore),
    ('/update-rating', UpdateRating),
    ('/im-feeling-lucky', FeelingLucky),
    ('/top-five', TopFive),
    ('/sign-user', SignUser),
    ('/user-images', UserImages),
    ('/intersect-lists', intersectLists),
    ('/new-images', newImages),
    ('/proper-delete', ProperDelete)
], debug=True)

def main():
    random.seed()       
    run_wsgi_app(application)
    
if __name__ == '__main__':
    main()