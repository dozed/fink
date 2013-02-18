#!/bin/bash

export FINK_BASE="http://localhost:8080"

curl $FINK_BASE/admin/api/posts -H "Content-type: application/json" --data @data/post1.json
curl $FINK_BASE/admin/api/posts -H "Content-type: application/json" --data @data/post2.json
curl $FINK_BASE/admin/api/pages -H "Content-type: application/json" --data @data/page1.json
curl $FINK_BASE/admin/api/settings -X PUT -H "Content-type: application/json" --data @data/settings.json
