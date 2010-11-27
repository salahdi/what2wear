import logging

#import cgi
#import sys #import sys module for printing to stdout


from google.appengine.ext import db
#from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
#from google.appengine.ext.webapp import template
from google.appengine.api import images
#from google.appengine.api import urlfetch
from django.utils import simplejson
import models

logging.getLogger().setLevel(logging.DEBUG)

class MainPage(webapp.RequestHandler):
    
    def get(self):
        self.response.out.write("""<html><body>
                                    welcome
                                    </body></html>""")

        #self.response.out.write("""<html><body>
        #                            admin: in order to load data please press the load button<br>
        #                            <div class="mybutton">    
        #                            <button onclick="window.location='/loadImages'" 
        #                            style="width:80;height:24">Load</button><br> 
        #                            </div>
        #                            </body></html>""")

        
class GetImageByKeyID (webapp.RequestHandler):
    
    def get(self):
        """this method receives an image id key and return the url address of the suitable image"""
        image_struct = db.get(self.request.get("key_id"))
        if image_struct.image:       
            self.response.headers['Content-Type'] = "application/json" 
            return_str = []
            """ send the url address of the requested image"""
            return_str[0] = {"url_id" : images.get_serving_url(image_struct.image.key())}
            self.response.out.write(simplejson.dumps(return_str))
        else:
            self.response.out.write("No image")


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
        types_list = []
        colors_list = []
        for i in range(items_num):
            types_list.append(str(self.request.get("item"+str(i+1)+"_type_id")))
            colors_list.append(str(self.request.get("item"+str(i+1)+"_color_id")))
        style = str(self.request.get("style_id"))
        season = str(self.request.get("season_id"))
    
        """create the list that will be returned from the request (in json format)"""
        imageList = []

        """filter according to gender"""
        all_images = models.ImageStruct.gql("WHERE subject_gender = :1", gender)
        """filter according to style, if needed"""
        if (style != ''):
            all_images.filter("style =", style)
        """filter according to season, if needed"""
        if (season != ''):
            all_images.filter("season =", season)
        all_images.order('-avg_image_rating')
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
                tempZip = zip(["post"], [cur_image.get_dict()])
                tempDict = dict(tempZip)
                imageList.append(tempDict)
                #imageList.append(cur_image.get_dict())

        postsZip = zip(["posts"], [imageList])
        postsDict = dict(postsZip)
        self.response.headers['Content-Type'] = "application/json"
        self.response.out.write(simplejson.dumps(postsDict))
#        self.response.headers['Content-Type'] = "application/json"
#        self.response.out.write(simplejson.dumps(imageList))        
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
        return_str = []
        return_str[0] = {"rating_id" : image_struct.avg_image_rating}
        self.response.out.write(simplejson.dumps(return_str))
        

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