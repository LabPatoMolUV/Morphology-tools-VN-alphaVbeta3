/*
 * Script used for ROI extraction, needed for posterior vitronectin detection due to the size of the whole slide histology images.
 * Fill the parameters and run
 * 
 * Paper:
 * @authors:Mara Stoks and Isaac Vieco-Martí
 */
 
 
//Define destination folder for ROIs
def path= "" 

//Define the size of the tiles in microns
def tileSize = 1000

//Define the downsample of the image. If it is set to 1, no compression will be done
def downsample = 1

//Define the image format.
def imgFormat = ".tif"


//Staining
setImageType('BRIGHTFIELD_H_DAB');
setColorDeconvolutionStains('{"Name" : "H-DAB default", "Stain 1" : "Hematoxylin", "Values 1" : "0.65111 0.70119 0.29049", "Stain 2" : "DAB", "Values 2" : "0.26917 0.56824 0.77759", "Background" : " 255 255 255"}');

//Trim the ROI in tiles
selectAnnotations();
runPlugin('qupath.lib.algorithms.TilerPlugin', '{"tileSizeMicrons": '+tileSize+',  "trimToROI": true,  "makeAnnotations": true,  "removeParentAnnotation": true}');

//for loop to save each ROI
def name = getProjectEntry().getImageName()
i=0
for( annotation in getAnnotationObjects()){
    def server = getCurrentServer()
    def roi = annotation.getROI()
    def requestROI = RegionRequest.createInstance(server.getPath(), downsample, roi)
    i++
    def outputPath= buildFilePath(path,  name + '_Region_ ' + i + imgFormat)
    writeImageRegion(server, requestROI, outputPath)
    }