from google.appengine.ext import db
from google.appengine.api import users
import random 
import sys
import logging

def get_user_by_email(user_email):
    user = users.User(user_email)
    if user is None:
        return None
    else:
        query = db.GqlQuery("SELECT * FROM UserStruct WHERE user = :1", user);
        return query.get()


class UserStruct(db.Model):
    user = db.UserProperty(required=True);
    #gender = db.StringProperty(choices = set(['male', 'female']))
    #location = db.StringProperty();
    score = db.FloatProperty(default = float(0))
    images_num = db.IntegerProperty(default = 0)
 
    def increase_images_num(self):
        self.images_num = self.images_num + 1
        self.put()
           
    def update_score(self, old_rating, new_rating):
        self.score = self.score - old_rating + new_rating
        self.put()
        
    def to_dict(self):
        d = {}
        this_user = self.user
        d['email_id'] = this_user.email()
        d['nickname_id'] = this_user.nickname()
        d['score_id'] = "%.2f" % self.score
        d['images_num_id'] = self.images_num
        return d
    
    
class ImageStruct(db.Model):
    image = db.BlobProperty()
    subject_gender = db.StringProperty(default = 'male', choices = set(['male', 'female']))
    avg_image_rating = db.FloatProperty(default = float(0))
    rating_sum = db.FloatProperty(default = float(0))
    rating_num = db.IntegerProperty(default = 0)
    items_num = db.IntegerProperty(default = 0, choices = set([0, 1, 2, 3, 4]))
    items_list = db.StringListProperty() #a list of strings in form type,color
    season = db.StringProperty(default = '', choices = set(['','Summer', 'Autumn', 'Winter', 'Spring']))
    style = db.StringProperty(default = '', choices = set(['','Casual', 'Elegant', 'Sports']))
    random_num = db.IntegerProperty()
    user = db.ReferenceProperty(UserStruct)
    
    def to_dict(self):
        """ this method returns a dictionary of some of the properties above """
        d = {}
        d['key_id'] = str(self.key())
        d['gender_id'] = self.subject_gender
        d['season_id'] = self.season
        d['style_id'] = self.style
        d['rating_id'] = "%.2f" % self.avg_image_rating
        if self.user:
            this_user_struct = self.user
            this_user = this_user_struct.user
            d['nickname_id'] = this_user.nickname()
            d['email_id'] = this_user.email() #delete this?
        d['items_num_id'] = str(self.items_num)
        # add the items to the dictionary
        i = 1
        for item in self.items_list:
            d.update(self.item_to_dict(item, i))
            i = i + 1
        return d
    
    def item_to_dict(self, item, index):
        """ this method returns a dictionary of the type and color properties """
        d = {}
        list = item.split(",")
        d["item" + str(index) + "_type_id"] = str(list[0])
        d["item" + str(index) + "_color_id"] = str(list[1])
        return d
    
    def put(self):
        if not self.random_num:
            """if we didn't assign a random number yet- assign one"""
            isFound = 0
            rand = -1
            """get random number"""
            while (isFound == 0):
                rand = random.randint(1, sys.maxint)
                """check if we already used this random number"""
                query = db.Query(ImageStruct)
                result = query.filter('random_num =', rand).get()
                if (result is None):
                    isFound = 1         
            self.random_num = rand
        super(ImageStruct, self).put()
        
    def assign_user(self, user_email):
        if not self.user:
            user_struct = get_user_by_email(user_email)
            if user_struct:
                """increase the user images_num"""
                user_struct.increase_images_num()
                """save the user as the uploader user"""
                self.user = user_struct
                self.put()
