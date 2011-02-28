import models
import random
import sys
import os
os.environ['DJANGO_SETTINGS_MODULE'] = 'settings'

from google.appengine.dist import use_library
use_library('django', '1.2')


from google.appengine.ext import db
from google.appengine.api import images
from google.appengine.api import urlfetch
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext.webapp import template
from google.appengine.api import users

""" This method receives a length len and returns an array of this length filled
with random numbers """
def rundom_num_arr(len):
    i = 1
    lst = [random.randint(1, sys.maxint)]
    while (i<len):
        isFound = 0
        rand = -1
        """get random number"""
        while (isFound == 0):
            rand = random.randint(1, sys.maxint)
            """check if we already used this random number"""
            if not (rand in lst):
                isFound = 1
        lst.append(rand)   
        i += 1      
    return lst


class LoadPage(webapp.RequestHandler):
    
    """ This method receives a status of the loading operation and redirects the web page to a page
        displaying the operation status- success, nothing (=data already loaded) or failed  """
    def get(self):
        status = self.request.get("status");
        line = ''
        if status:
            if (status == "success"):
                line = 'Data loaded successfully'
            elif (status == "nothing"):
                line = 'Data already loaded'
            elif (status == "failed"):
                line = 'Data loading failed'
        
        user = users.GetCurrentUser()
        login = users.CreateLoginURL(self.request.uri)
        logout = users.CreateLogoutURL(self.request.uri)

        template_file_name = 'resultpage.html'
        template_values = {'login': login, 'logout': logout, 'user': user, 'line': line}

        path = os.path.join(os.path.dirname(__file__), template_file_name)
        self.response.out.write(template.render(path, template_values))

         
class ClearPage(webapp.RequestHandler):
    
    """ This method receives a status of the clear data operation and redirects the web page to 
        a page displaying the operation status- success or denied. """   
    def get(self):
        status = self.request.get("status");
        line = ''
        if status:
            if (status == "success"):          
                line = 'Data cleared successfully'
            elif (status == "denied"):
                line = 'Access denied'
        
        user = users.GetCurrentUser()
        login = users.CreateLoginURL(self.request.uri)
        logout = users.CreateLogoutURL(self.request.uri)

        template_file_name = 'resultpage.html'
        template_values = {'login': login, 'logout': logout, 'user': user, 'line': line}

        path = os.path.join(os.path.dirname(__file__), template_file_name)
        self.response.out.write(template.render(path, template_values))

class LoadData(webapp.RequestHandler):
    
    """ This method loads initial data to the application, according to the data in my_images_data.csv 
    and the images in my_images folder.
    The images are assigned with initial random rating """
    def get(self):
        query = models.LoaderStruct.all()
        result = query.get()
        loaded = False
        if result:   
            if (result.isLoaded):
                status = "nothing"
                loaded = True
        else:
            key = models.LoaderStruct().put()
            result = db.get(key)
        if not loaded:
            """create user structs"""
            models.UserStruct(email_or_id = "michalfaktor@gmail.com", name="michalfaktor" ,account_type ="google").put()
            models.UserStruct(email_or_id = "cohenhila4@gmail.com",  name= "cohenhila4" ,account_type ="google").put() 
            models.UserStruct(email_or_id = "niritg@gmail.com", name = "niritg", account_type ="google").put()
            """open CSV file"""
            reader = urlfetch.Fetch('http://what-2-wear-tester.appspot.com/static/my_images_data.csv').content
            reader = reader.split("\r\n")
            counter = 0      
            headlines = reader[0].split(",")
            j = 0
            randsLst = rundom_num_arr(len(reader))
            putList = []
            for row in reader:
                if (counter>0):
                    content =  row.split(",")
                    combo = zip(headlines, content)
                    d = dict(combo)
                    if (d['image_name'] != ''):
                        url = 'http://what-2-wear-tester.appspot.com/my_images/' + d['image_name']
                        """ get a random rating to start with"""
                        rand_rating = float(random.randint(1, 5))
                        imageBlob = models.ImageStruct(image = db.Blob(images.resize(urlfetch.Fetch(url).content, 300, 300)))
                        imageBlob.put()
                        if (d['season'] == ''):
                            season = []
                        else:
                            season = [d['season']]
                        if (d['style'] == ''):
                            style = []
                        else:
                            style = [d['style']]
                        image = models.ImageMetadataStruct(image = imageBlob, 
                                                           random_num = randsLst[counter],
                                                           subject_gender = d['gender'],
                                                           items_num = int(d['items_num']),
                                                           style = style,
                                                           season = season,
                                                           rating_sum = rand_rating,
                                                           rating_num = 1,
                                                           avg_image_rating = rand_rating)
                        for i in range(image.items_num):
                            (image.items_list).append(d['item'+str(i+1)+'_type']+","+d['item'+str(i+1)+'_color'])
                        """assign a user to the image struct"""
                        if (j == 0):
                            user = image.assign_user("michalfaktor@gmail.com")
                            j = j + 1
                        elif (j == 1):
                            user = image.assign_user("cohenhila4@gmail.com")
                            j = j + 1
                        else:
                            user = image.assign_user("niritg@gmail.com")
                            j = 0
                        user.update_score(0, image.avg_image_rating)
                        putList.append(image)
                    else:
                        break
                counter += 1
            db.put(putList)
            result.isLoaded = True
            result.put()
            status = "success"
        self.redirect("/load-page?status="+status)

class ClearAllData(webapp.RequestHandler):
    
    """ This method clears all data from the data store """
    def get(self):
        user = users.GetCurrentUser()
        if (user.nickname() != "michalfaktor"):
            status = "denied"      
        else:
            """clear all data in the data store"""
            allImagesMetadata = models.ImageMetadataStruct.all()
            if allImagesMetadata:
                db.delete(allImagesMetadata)
            allImages = models.ImageStruct.all()
            if allImages:
                db.delete(allImages)
            allUsers = models.UserStruct.all()
            if allUsers:
                db.delete(allUsers)
            """update the loader struct"""
            query = models.LoaderStruct.all()
            result = query.get()
            if result:
                result.isLoaded = False
                result.put()
            status = "success"
        self.redirect("/clear-page?status="+status)     


application = webapp.WSGIApplication([
    ('/loadData', LoadData),
    ('/clearAllData', ClearAllData),
    ('/load-page', LoadPage),
    ('/clear-page', ClearPage)
], debug=True)

def main():
    run_wsgi_app(application)

if __name__ == '__main__':
    main()     