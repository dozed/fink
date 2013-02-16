#!/bin/bash

export FINK_BASE="http://localhost:8080"

curl $FINK_BASE/admin/api/posts -H "Content-type: application/json" --data @data/post1.json
curl $FINK_BASE/admin/api/posts -H "Content-type: application/json" --data @data/post2.json
