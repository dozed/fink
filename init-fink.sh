#!/bin/bash

export FINK_BASE="http://localhost:8080"

curl $FINK_BASE/admin/api/categories -H "Content-type: application/json" --data "{\"id\": 0, \"name\": \"foo cat\"}"
curl $FINK_BASE/admin/api/categories -H "Content-type: application/json" --data "{\"id\": 0, \"name\": \"foo bar\"}"
curl $FINK_BASE/admin/api/posts -H "Content-type: application/json" --data "{\"id\":0,\"date\":1343082270993,\"catId\":2,\"title\":\"askdfaskdf\",\"author\":\"asodk\",\"text\":\"asdfj\",\"category\":{\"id\":2,\"name\":\"foo bar\",\"jsonClass\":\"Category\"},\"tags\":[{\"id\":0,\"name\":\"jafe\"}]}"
curl $FINK_BASE/admin/api/tags -H "Content-type: application/json" --data "{\"id\": 0, \"name\": \"bar\"}"
curl $FINK_BASE/admin/api/galleries -H "Content-type: application/json" --data "{\"images\":[],\"tags\":[{\"jsonClass\":\"Tag\",\"id\":1,\"name\":\"ab\"},{\"jsonClass\":\"Tag\",\"id\":2,\"name\":\"ab256\"}],\"id\":1,\"coverId\":0,\"date\":1343236437521,\"title\":\"adas\",\"author\":\"w3b\",\"shortlink\":\"6w3b6\",\"text\":\"a65\"}"
