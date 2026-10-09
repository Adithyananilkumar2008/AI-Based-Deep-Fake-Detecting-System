from http.server import HTTPServer, SimpleHTTPRequestHandler
import sys
import os

class CustomHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory="www", **kwargs)

    def log_message(self, format, *args):
        sys.stderr.write("%s - - [%s] %s\n" %
                         (self.address_string(),
                          self.log_date_time_string(),
                          format%args))

if __name__ == '__main__':
    server_address = ('0.0.0.0', 3000)
    httpd = HTTPServer(server_address, CustomHandler)
    print("Serving on port 3000...")
    sys.stdout.flush()
    httpd.serve_forever()
