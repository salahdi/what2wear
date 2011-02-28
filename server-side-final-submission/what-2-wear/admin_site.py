import models

import os
os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'

from google.appengine.dist import use_library
use_library('django', '1.2')

from django.utils import simplejson

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db


class SitePage(webapp.RequestHandler):
    
    """ This method displays a page with all the images and images details the users uploaded"""
    def get(self):
        user = users.GetCurrentUser()
        login = users.CreateLoginURL(self.request.uri)
        logout = users.CreateLogoutURL(self.request.uri)

        template_file_name = 'contentpage.html'
        template_values = {'login': login, 'logout': logout, 'user': user}

        path = os.path.join(os.path.dirname(__file__), template_file_name)
        self.response.out.write(template.render(path, template_values))


class GetImagesJson(webapp.RequestHandler):
    
    """ This method receives a page number (optional) and returns a JSON with images details (to
    be displayed in a web page). The images are sorted by date in decreasing order.
    The JSON response consists of a total images number (with key 'total') and a list of images
    (with key 'images').
    Each image is a dictionary with the following keys:
    key, date, gender, seasons, styles, rating, name, items and pic.
    pic is the relate url for the image"""
    def get(self):
        page = self.request.get('page')
        if (not page):
            page = 1
        else:
            page = int(page)
        results = db.GqlQuery("SELECT * FROM ImageMetadataStruct ORDER BY date DESC")
        item_per_page = 8
        start_index = (page - 1) * item_per_page
        total_items = results.count()
        results = results.fetch(item_per_page, start_index)   
        json_obj = {}
        json_obj['total'] = total_items
        json_obj['images'] = []
        
        for result in results:
            imageData = {}
            imageData['key'] = str(result.key())
            imageData['date'] = result.date.ctime()
            imageData['gender'] = result.subject_gender
            imageData['seasons'] = ", ".join(result.season)
            imageData['styles'] = ", ".join(result.style)
            imageData['rating'] = "%.2f" % result.avg_image_rating
            user = result.user
            imageData['name'] = user.name
            items = []
            for item in result.items_list:
                itemTemp = item.replace(" ", "&nbsp;")
                temp = itemTemp.split(",")
                items.append(temp[1] + "&nbsp;" + temp[0])
            imageData['items'] = ", ".join(items)
            imageData['pic'] = 'img?image_key_id='+str((result.image).key())
                
            json_obj['images'].append(imageData)

        json_str = simplejson.dumps(json_obj)

        self.response.headers['Content-Type'] = 'text/javascript'
        self.response.out.write(json_str)

class DeleteImages(webapp.RequestHandler):
    
    """ This method deletes the entities of type ImageMetadataStruct with the given keys,
    and also deletes the ImageStruct entities that the given entities points to (as reference
    property)"""
    def post(self):
        imageKeys = self.request.get('keys', allow_multiple = True)
        page = self.request.get('page')
        
        list1 = []
        list2 = []
        for key in imageKeys:
            entity = db.get(key)
            if (entity.kind() == "ImageMetadataStruct"):
                """delete from the user who uploaded the image all related data"""
                user = entity.user
                user.score -= entity.avg_image_rating
                user.images_num -= 1
                user.put()    
                list1.append(entity.image)
                list2.append(entity)
        db.delete(list1)
        db.delete(list2)
        
        self.response.headers['Content-Type'] = 'text/html'
        self.response.out.write(self.request.uri)
        
        if page:
            self.redirect("/view-content?page=" + page)
        else:
            self.redirect("/view-content")
            

application = webapp.WSGIApplication([
    ('/view-content', SitePage),
    ('/imagesJson', GetImagesJson),
    ('/delete', DeleteImages)
], debug=True)

def main():
    run_wsgi_app(application)

if __name__ == '__main__':
    main()   
