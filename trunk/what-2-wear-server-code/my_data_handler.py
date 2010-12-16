from google.appengine.ext import db
from google.appengine.api import images
from google.appengine.api import urlfetch
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.api import users
from google.appengine.ext.webapp.util import login_required
import models


class LoadImages(webapp.RequestHandler):
    @login_required
    def get(self):
        self.response.out.write("""<html><body>""")
        user = users.GetCurrentUser()
        if (user.nickname() != "michalfaktor"):
            self.response.out.write("""access denied<br>""")				
        else:
            reader = urlfetch.Fetch('http://what-2-wear.appspot.com/my_csv_file/my_images_data.csv').content
            reader = reader.split("\r\n")
            isFirst = 1      
            headlines = reader[0].split(",")
            for row in reader:
                if (isFirst != 1):
                    content =  row.split(",")
                    combo = zip(headlines, content)
                    d = dict(combo)
                    if (d['image_name'] != ''):
                        url = 'http://what-2-wear.appspot.com/my_images/' + d['image_name']
                        image = models.ImageStruct(image = db.Blob(images.resize(urlfetch.Fetch(url).content, 190, 190)), 
                                                   subject_gender = d['gender'],
                                                   items_num = int(d['items_num']),
                                                   style = d['style'],
                                                   season = d['season'])
                        image.assign_random()
                        image.put()
                        for i in range(image.items_num):
                            item = models.ItemStruct(image_struct = image, 
                                                     item_type = d['item'+str(i+1)+'_type'], 
                                                     item_color = d['item'+str(i+1)+'_color'])
                            item.put()
                    else:
                        break
                else:
                    """skip the first row"""
                    isFirst = 0
            self.response.out.write("""data loaded<br>""")
        self.response.out.write("""<div class="mybutton">    
                               <button onclick="window.location='/'" 
                               style="width:80;height:24">return</button><br> 
                               </div>
                               </body></html>""")

class ClearImages(webapp.RequestHandler):
    @login_required
    def get(self):
        self.response.out.write("""<html><body>""")
        user = users.GetCurrentUser()
        if (user.nickname() != "michalfaktor"):
            self.response.out.write("""access denied<br>""")				
        else:
            """clear all data store"""
            allImages = models.ImageStruct.all()
            if allImages:
                db.delete(allImages)
            allItems = models.ItemStruct.all()
            if allItems:
                db.delete(allItems)
            self.response.out.write("""all data has been deleted<br>""")
        self.response.out.write("""<div class="mybutton">    
                                   <button onclick="window.location='/'" 
                                   style="width:80;height:24">return</button><br> 
                                   </div>
                                   </body></html>""")       


application = webapp.WSGIApplication([
    ('/loadImages', LoadImages),
    ('/clearImages', ClearImages)
], debug=True)

def main():
    run_wsgi_app(application)

if __name__ == '__main__':
    main()     