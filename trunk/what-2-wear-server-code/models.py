from google.appengine.ext import db

class ImageStruct(db.Model):
    #uploader_user = db.UserProperty() #delete this property???
    image = db.BlobProperty()
    subject_gender = db.StringProperty(default = 'male', choices = set(['male', 'female']))
    avg_image_rating = db.FloatProperty(default = float(0))
    rating_sum = db.FloatProperty(default = float(0))
    rating_num = db.IntegerProperty(default = 0)
    items_num = db.IntegerProperty(default = 0, choices = set([0, 1, 2, 3, 4]))
    season = db.StringProperty(default = '', choices = set(['','Summer', 'Autumn', 'Winter', 'Spring']))
    style = db.StringProperty(default = '', choices = set(['','Casual', 'Elegant', 'Sports']))
    
    
    def to_dict(self):
        """ this method returns a dictionary of some of the properties above """
        d = {}
        d['key_id'] = str(self.key())
        d['gender_id'] = self.subject_gender
        d['season_id'] = self.season
        d['style_id'] = self.style
        d['rating_id'] = str(self.avg_image_rating)
        d['items_num_id'] = str(self.items_num)
        # add the items to the dictionary
        i = 1
        for item in self.itemstruct_set:
            d.update(item.to_dict(i))
            i = i + 1
        return d
    
class ItemStruct(db.Model):
    item_type = db.StringProperty(default = 'Coat', choices = set(['Coat', 'Jacket', 'Dress', 'Skirt', 'Trousers',
                                                                'Jeans', 'Knitwear', 'Shirt', 'T-Shirt',
                                                                'Shoes', 'Scarf']))
    item_color = db.StringProperty(default = 'Black', choices = set(['Black', 'Blue', 'Light Blue', 'Green', 'Grey',
                                                                 'Light Green', 'Turquoise', 'Purple',
                                                                 'Light Purple', 'Red', 'Pink', 'Yellow',
                                                                 'Brown', 'Beige', 'Mastered', 'White', 'Multi-colored',
                                 'Dark Blue', 'Scarlet']))
    image_struct = db.ReferenceProperty(ImageStruct, required=True)
    
    
    def to_dict(self, index):
        """ this method returns a dictionary of the type and color properties listed above.
        index is the item's number in the items list"""
        d = {}
        d["item" + str(index) + "_type_id"] = self.item_type      
        d["item" + str(index) + "_color_id"] = self.item_color
        return d