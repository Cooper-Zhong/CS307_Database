import json
filename = 'posts.json' # the path of json file
with open(filename) as f:
    posts = json.load(f)

for post in posts:
    print(type(post)) # it is a dist type
    print(post['Post ID'])
    print(json.dumps(post, indent=1))