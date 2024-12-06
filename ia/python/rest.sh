# FROM https://ai.google.dev/gemini-api/docs/document-processing?lang=node

BASE_URL="https://generativelanguage.googleapis.com"
PDF_PATH=/src/androidTest/assets/test.pdf
GOOGLE_API_KEY=$1

echo "[START files_create_pdf]"
# [START files_create_pdf]
NUM_BYTES=$(wc -c < "${PDF_PATH}")
DISPLAY_NAME=TEXT
tmp_header_file=upload-header.tmp

# Initial resumable request defining metadata.
# The upload url is in the response headers dump them to a file.
curl "${BASE_URL}/upload/v1beta/files?key=${GOOGLE_API_KEY}" \
  -D upload-header.tmp \
  -H "X-Goog-Upload-Protocol: resumable" \
  -H "X-Goog-Upload-Command: start" \
  -H "X-Goog-Upload-Header-Content-Length: ${NUM_BYTES}" \
  -H "X-Goog-Upload-Header-Content-Type: application/pdf" \
  -H "Content-Type: application/json" \
  -d "{'file': {'display_name': '${DISPLAY_NAME}'}}" 2> /dev/null

upload_url=$(grep -i "x-goog-upload-url: " "${tmp_header_file}" | cut -d" " -f2 | tr -d "\r")
rm "${tmp_header_file}"

# Upload the actual bytes.
curl "${upload_url}" \
  -H "Content-Length: ${NUM_BYTES}" \
  -H "X-Goog-Upload-Offset: 0" \
  -H "X-Goog-Upload-Command: upload, finalize" \
  --data-binary "@${PDF_PATH}" 2> /dev/null > file_info.json

file_uri=$(jq ".file.uri" file_info.json)
echo file_uri=$file_uri

# Now generate content using that file
curl "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$GOOGLE_API_KEY" \
    -H 'Content-Type: application/json' \
    -X POST \
    -d '{
      "contents": [{
        "parts":[
          {"text": "Summarize the recipe from this document. Present the data under JSON format. Here is the schema:\n{\"recipe\": RECIPE, \"ingredients\": list[INGREDIENT], \"steps\": list[STEP]}\n\nRECIPE = {\"name\": str, \"image_url\": str}\nINGREDIENT = {\"name\": str, \"quantity\": double, \"unit\": str}\nsteps = {\"description\": str, list[INGREDIENT]}\n\"image_url\" is optional. All other fields are required.\nImportant: Only return a single piece of valid JSON text."},
          {"file_data":{"mime_type": "application/pdf", "file_uri": '$file_uri'}}]
        }]
       }' 2> /dev/null > response.json

cat response.json
echo

jq ".candidates[].content.parts[].text" response.json
# [END files_create_pdf]