# Welcome to Cloud Functions for Firebase for Python!
# To get started, simply uncomment the below code or create your own.
# Deploy with `firebase deploy`
import sys
from pathlib import Path
sys.path.insert(0, Path(__file__).parent.as_posix())

from firebase_functions import https_fn
from firebase_functions import logger
from google import genai
from google.genai import types
import tempfile
import os
import typing
import base64
import json
import requests

client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))

API_KEY = os.getenv("API_KEY")
AI_MODEL = "gemini-2.0-flash"
JINA_API_KEY = os.getenv("JINA_API_KEY")

@https_fn.on_call(region="europe-west2")
def extractRecipe(req: https_fn.CallableRequest) -> typing.Any:

    if not "apiKey" in req.data != API_KEY:
        return {"error": {"message" : "Unauthorized", "status": 403}}

    if not "pdfData" in req.data and not "urlData" in req.data:
        return {"error": {"message" : "Missing mandatory data: pdfData or urlData", "status": 400}}

    if not "locale" in req.data:
        return {"error": {"message" : "Missing mandatory data: locale", "status": 400}}

    tmp = None
    if "pdfData" in req.data:
        if not "name" in req.data:
            return {"error": {"message" : "Missing mandatory data: name", "status": 400}}

        tmp = tempfile.NamedTemporaryFile(mode="w",suffix=".pdf",delete=False)

        base64_str = req.data["pdfData"]

        with open(tmp.name, 'wb') as f:
            f.write(base64.decodebytes(bytes(base64_str, "utf-8")))

        data_payload = client.files.upload(file=tmp.name,  config=types.UploadFileConfig(display_name=req.data["name"]))
    else :
        data_payload = requests.get(f'https://r.jina.ai/{req.data["urlData"]}', headers={"Authorization": "Bearer "+JINA_API_KEY}).text
        logger.debug(req.data["locale"])

    response = client.models.generate_content(model= AI_MODEL,
        contents=["Summarize the recipe from this document. Present the data under JSON format. Here is the schema:\n" +
         "{\"recipe\": RECIPE, \"ingredients\": list[INGREDIENT], \"steps\": list[STEP]}\n\n" +

         "RECIPE = {\"name\": str, \"image_url\": str}\n" +
         "INGREDIENT = {\"name\": str, \"quantity\": double, \"unit\": str}\n" +
         "steps = {\"description\": str, ingredients: list[INGREDIENT]}\n" +
         "\"image_url\" is optional. All other fields are required.\n" +
         "step.ingredients is the list of ingredients used for this step\n" +
         "\"unit\" is one of these values: Gramme, Kilogramme, Milliliter, Liter, TeaSpoon, TableSpoon, Cup, Pinch, or null if nothing correspond\n" +
         "Important: Only return a single piece of valid JSON text." +
         f'Translate it the output JSON into {req.data["locale"]}  if necessary.', data_payload])

    if tmp is not None:
        tmp.close()
        client.files.delete(name=data_payload.name)

    json_str = response.candidates[0].content.parts[0].text.replace("```json", "").replace("```", "")

    return {"result": json.loads(json_str)}
"""
@https_fn.on_call(region="europe-west2")
def generateIngredientImage(req: https_fn.CallableRequest) -> typing.Any:
    name = req.data["name"]
    return imagen.generate_images(
        prompt="a hand drawn" + name + "on a plain green background, drawing",
        number_of_images=1,
        safety_filter_level="block_only_high",
        person_generation="allow_adult",
        aspect_ratio="1:1",
        #negative_prompt="Outside",
    ).images
    
    """