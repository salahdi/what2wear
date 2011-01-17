from google.appengine.ext import db
import random 
import sys

def get_user_by_email_or_id(email_or_id):
    query = db.GqlQuery("SELECT * FROM UserStruct WHERE email_or_id = :1", email_or_id);
    return query.get()

def get_unique_random_num():
    isFound = 0
    rand = -1
    """get random number"""
    while (isFound == 0):
        rand = random.randint(1, sys.maxint)
        """check if we already used this random number"""
        query = db.Query(ImageMetadataStruct)
        result = query.filter('random_num =', rand).get()
        if (result is None):
            isFound = 1         
    return rand

"""a structure to hold all the information of a certain user of the application"""
class UserStruct(db.Model):
    name = db.StringProperty();
    email_or_id = db.StringProperty(required = True);
    account_type = db.StringProperty(required = True, choices = set(['google', 'facebook']));
    score = db.FloatProperty(default = float(0))
    images_num = db.IntegerProperty(default = 0)
 
    """a method to increase the images number counter"""
    def increase_images_num(self):
        self.images_num = self.images_num + 1
        self.put()
           
    """a method to update an user score"""
    def update_score(self, old_rating, new_rating):
        """replace an old image rating with its new one"""
        self.score = self.score - old_rating + new_rating
        self.put()
        
    """a method that turns the data of the user to a dictionary struct"""
    def to_dict(self):
        d = {}
        d['name_id'] = self.name
        d['email_or_id_id'] = self.email_or_id
        d['score_id'] = "%.2f" % self.score
        d['images_num_id'] = self.images_num
        return d

"""a structure to hold an image """    
class ImageStruct(db.Model):
    image = db.BlobProperty()

"""a structure to hold an image metadata (image details)"""    
class ImageMetadataStruct(db.Model):
    image = db.ReferenceProperty(ImageStruct)
    date = db.DateTimeProperty(auto_now_add = True)
    subject_gender = db.StringProperty(default = 'male', choices = set(['male', 'female']))
    items_num = db.IntegerProperty(default = 0, choices = set([0, 1, 2, 3, 4]))
    items_list = db.StringListProperty() #a list of strings in form type,color
    season = db.StringListProperty()
    style = db.StringListProperty()
    random_num = db.IntegerProperty()
    user = db.ReferenceProperty(UserStruct)
    
    avg_image_rating = db.FloatProperty(default = float(0))
    rating_sum = db.FloatProperty(default = float(0))
    rating_num = db.IntegerProperty(default = 0)
    
    def to_dict(self):
        """ this method returns a dictionary of some of the properties above """
        d = {}
        d['key_id'] = str(self.key())
        d['image_key_id'] = str(ImageMetadataStruct.image.get_value_for_datastore(self))
        d['gender_id'] = self.subject_gender
        d['season_id'] = ",".join(self.season)
        d['style_id'] = ",".join(self.style)
        d['rating_id'] = "%.2f" % self.avg_image_rating
        if self.user:
            this_user = self.user
            d['email_or_id_id'] = this_user.email_or_id
            d['name_id'] = this_user.name
        d['items_num_id'] = str(self.items_num)
        # add the items to the dictionary
        i = 1
        for item in self.items_list:
            d.update(self.item_to_dict(item, i))
            i = i + 1
        return d

    """ this method returns a dictionary of the type and color properties """
    def item_to_dict(self, item, index):    
        d = {}
        list = item.split(",")
        d["item" + str(index) + "_type_id"] = str(list[0])
        d["item" + str(index) + "_color_id"] = str(list[1])
        return d
    
    """add a random number to the image struct (only once)"""
    def put(self):
        if not self.random_num:
            """if we didn't assign a random number yet- assign one"""
            isFound = 0
            rand = -1
            """get random number"""
            while (isFound == 0):
                rand = random.randint(1, sys.maxint)
                """check if we already used this random number"""
                query = db.Query(ImageMetadataStruct)
                result = query.filter('random_num =', rand).get()
                if (result is None):
                    isFound = 1         
            self.random_num = rand
        super(ImageMetadataStruct, self).put()
        
    """this method adds a certain user as the owner of a certain image struct.
    the method updates the number of images the user owns"""
    def assign_user(self, email_or_id):
        if not self.user:
            user_struct = get_user_by_email_or_id(email_or_id)
            if user_struct:
                """increase the user images_num"""
                db.run_in_transaction(UserStruct.increase_images_num, user_struct)
                """save the user as the uploader user"""
                self.user = user_struct
                return user_struct
    
class LoaderStruct(db.Model):
    isLoaded = db.BooleanProperty(default = False)
    