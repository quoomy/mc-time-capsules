#!/usr/bin/env python3

import cgi
import os
import sys
import json
import time

BASE_UPLOAD_DIR = '/var/www/timecapsules.quoomy/uploads'
UNMONITORED_DIR = os.path.join(BASE_UPLOAD_DIR, 'unmonitored')
USER_UPLOAD_JSON = os.path.join(BASE_UPLOAD_DIR, 'user_last_uploads.json')
LAST_ID_FILE = os.path.join(UNMONITORED_DIR, 'last_id.txt')  # track capsule ID
MAX_PNG_SIZE = 500 * 1024  # 500 KB in metric
MAX_TEXT_SIZE = 1000       # characters
ONE_MONTH_SECONDS = 30 * 24 * 3600

def load_user_timestamps():
	"""Load or create the JSON file with user timestamps."""
	if not os.path.exists(USER_UPLOAD_JSON):
		with open(USER_UPLOAD_JSON, 'w') as f:
			json.dump({}, f)
		return {}
	with open(USER_UPLOAD_JSON, 'r') as f:
		try:
			return json.load(f)
		except json.JSONDecodeError:
			return {}  # If it's corrupted, reset it
	return {}

def save_user_timestamps(timestamps):
	"""Save the user timestamp data back to JSON."""
	with open(USER_UPLOAD_JSON, 'w') as f:
		json.dump(timestamps, f)

def get_next_id():
	"""Retrieve and increment the last used ID."""
	if not os.path.exists(LAST_ID_FILE):
		with open(LAST_ID_FILE, 'w') as f:
			f.write("0")

	with open(LAST_ID_FILE, 'r+') as f:
		try:
			current_id = int(f.read().strip())
		except ValueError:
			current_id = 0
		next_id = current_id + 1
		f.seek(0)
		f.truncate()
		f.write(str(next_id))
	return next_id

def main():
	print("Content-Type: text/html\n")  # CGI content type
	form = cgi.FieldStorage()

	# Ensure presence of required fields
	if "png_file" not in form:
		print("<html><body><h2>PNG file is required!</h2></body></html>")
		return
	username = form.getvalue("username")
	if not username:
		print("<html><body><h2>Username is required!</h2></body></html>")
		return
	if "text_data" not in form:
		print("<html><body><h2>Text data is required!</h2></body></html>")
		return

	# Retrieve form fields
	png_item = form.getfirst("png_file")
	text_data = form.getfirst("text_data", "")
	username  = form.getfirst("username", "")
	signature = form.getfirst("signature", None)
	game_ver  = form.getfirst("game_version", "")
	modloader = form.getfirst("modloader", "")

	# Check PNG size and load actual file contents
	png_file_item = form["png_file"]
	if png_file_item.filename:
		png_data = png_file_item.file.read()
		if len(png_data) > MAX_PNG_SIZE:
			print(f"<html><body><h2>PNG exceeds 200 KB. Rejected. (ಠ_ಠ)</h2></body></html>")
			return
	else:
		print("<html><body><h2>No PNG file provided. (ಠ_ಠ)</h2></body></html>")
		return

	# Check text size
	if len(text_data) > MAX_TEXT_SIZE:
		print("<html><body><h2>Text too long (> 500 chars). (꒪⌓꒪)</h2></body></html>")
		return

	# Check user upload timestamps to prevent spam
	timestamps_dict = load_user_timestamps()
	now = int(time.time())  # current unix timestamp
	last_upload = timestamps_dict.get(username, 0)
	if (now - last_upload) < ONE_MONTH_SECONDS:
		print("<html><body><h2>You have already uploaded recently. Wait a while, dear friend. ʕ •̀ o •́ ʔ</h2></body></html>")
		return

	# If passes the spam check, update the user's timestamp
	timestamps_dict[username] = now
	save_user_timestamps(timestamps_dict)

	# Get next ID and create directory
	new_id = get_next_id()
	capsule_dir = os.path.join(UNMONITORED_DIR, str(new_id))
	os.makedirs(capsule_dir, exist_ok=True)

	# Save PNG
	png_path = os.path.join(capsule_dir, "image.png")
	with open(png_path, 'wb') as f:
		f.write(png_data)

	# Save text
	text_path = os.path.join(capsule_dir, "text.txt")
	with open(text_path, 'w') as f:
		f.write(text_data)

	# Save username
	user_path = os.path.join(capsule_dir, "username.txt")
	with open(user_path, 'w') as f:
		f.write(username)

	# Save optional signature
	if signature:
		sig_path = os.path.join(capsule_dir, "signature.txt")
		with open(sig_path, 'w') as f:
			f.write(signature)

	# Save timestamp
	ts_path = os.path.join(capsule_dir, "timestamp.txt")
	with open(ts_path, 'w') as f:
		f.write(str(now))

	# Save game version
	gv_path = os.path.join(capsule_dir, "gameversion.txt")
	with open(gv_path, 'w') as f:
		f.write(game_ver)

	# Save modloader
	ml_path = os.path.join(capsule_dir, "modloader.txt")
	with open(ml_path, 'w') as f:
		f.write(modloader)

	# Finally, respond with success
	print(f"<html><body><h2>Time Capsule #{new_id} uploaded successfully! (✿◕‿◕)</h2></body></html>")

if __name__ == "__main__":
	main()
