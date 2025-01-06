#!/usr/bin/env python3

import cgi
import os
import sys

# Configuration
UPLOAD_DIR = '/var/www/timecapsules.quoomy/uploads'

def main():
    print("Content-Type: text/html\n")
    form = cgi.FieldStorage()

    if "file" not in form:
        print("<html><body><h2>No file uploaded.</h2></body></html>")
        return

    file_item = form["file"]

    if file_item.filename:
        filename = os.path.basename(file_item.filename)
        file_path = os.path.join(UPLOAD_DIR, filename)

        # Prevent overwriting existing files
        if os.path.exists(file_path):
            print(f"<html><body><h2>File '{filename}' already exists.</h2></body></html>")
            return

        # Save the file
        with open(file_path, 'wb') as f:
            f.write(file_item.file.read())

        print(f"<html><body><h2>File '{filename}' uploaded successfully.</h2></body></html>")
    else:
        print("<html><body><h2>No file selected.</h2></body></html>")

if __name__ == "__main__":
    main()

