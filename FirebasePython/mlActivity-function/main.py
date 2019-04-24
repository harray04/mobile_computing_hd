import firebase_admin
import feature
from feature import write_to_file
from google.cloud import storage

def download_file(bucketName):
    bucket_name = 'fir-functions-17922.appspot.com'
    source_blob_name = bucketName
    destination_file_name = bucketName + '.csv'
    #DOWNLOAD
    storage_client = storage.Client()
    bucket = storage_client.get_bucket(bucket_name)
    blob = bucket.blob(source_blob_name)
    blob.download_to_filename(destination_file_name)

download_file('activity')

write_to_file('activity.csv')


