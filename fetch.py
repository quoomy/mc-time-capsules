#!/usr/bin/env python3

import os
import cgi
import json
import random
import base64

MONITORED_DIR = '/var/www/timecapsules.quoomy/uploads/monitored'

def read_file_if_exists(path, mode='r'):
    try:
        with open(path, mode) as f:
            return f.read()
    except FileNotFoundError:
        return ""


def main():
    print("Content-Type: text/html\n")

    if not os.path.exists(MONITORED_DIR) or not os.path.isdir(MONITORED_DIR):
        response = {"error": "Monitored folder not found. (╥_╥)"}
        print(json.dumps(response))
        return

    capsule_dirs = [
        d for d in os.listdir(MONITORED_DIR)
        if os.path.isdir(os.path.join(MONITORED_DIR, d))
    ]

    if not capsule_dirs:
        response = {"error": "No capsules found in 'monitored' folder! (ಠ_ಠ)"}
        print(json.dumps(response))
        return

    chosen_dir = random.choice(capsule_dirs)
    capsule_path = os.path.join(MONITORED_DIR, chosen_dir)

    username = read_file_if_exists(os.path.join(capsule_path, "username.txt")).strip()
    text_data = read_file_if_exists(os.path.join(capsule_path, "text.txt")).strip()
    signature = read_file_if_exists(os.path.join(capsule_path, "signature.txt")).strip()
    timestamp = read_file_if_exists(os.path.join(capsule_path, "timestamp.txt")).strip()
    game_version = read_file_if_exists(os.path.join(capsule_path, "gameversion.txt")).strip()
    modloader = read_file_if_exists(os.path.join(capsule_path, "modloader.txt")).strip()

    png_data = b""
    try:
        with open(os.path.join(capsule_path, "image.png"), "rb") as f:
            png_data = f.read()
    except FileNotFoundError:
        pass

    encoded_png = base64.b64encode(png_data).decode('utf-8') if png_data else None

    response = {
        "capsule_id": chosen_dir,
        "username": username,
        "text_data": text_data,
        "signature": signature,
        "timestamp": timestamp,
        "game_version": game_version,
        "mod_loader": modloader,
        "encoded_png": encoded_png or ""
    }

    print(json.dumps(response))

if __name__ == "__main__":
    main()
