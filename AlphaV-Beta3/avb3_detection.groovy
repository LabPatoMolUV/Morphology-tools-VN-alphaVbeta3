 /*
  * Script for automated αvβ3-positive cell detection. 
  * Manually remove areas necrosis, folds or other artifacts if needed
  * 
  * Paper: 
  * @authors:Mara Stoks and Isaac Vieco-Martí
  */ 


//Set path of interest to save the data
def path = "   " 

//Set the pixel classifier according to the staining
def pixelClassifier = "Tumor_detection_avb3"

//Set the minimum size to be detected (in microns)
def minimumSize = 40000


//Staining
setImageType('BRIGHTFIELD_H_DAB');
setColorDeconvolutionStains('{"Name" : "H-DAB default", "Stain 1" : "Hematoxylin", "Values 1" : "0.65111 0.70119 0.29049", "Stain 2" : "DAB", "Values 2" : "0.26917 0.56824 0.77759", "Background" : " 255 255 255"}');

//Tumor detection
resetSelection();
createAnnotationsFromPixelClassifier(pixelClassifier, minimumSize, 0.0) //if necessary, change detection parameters of pixel classifier

//Positive cell detection
selectAnnotations();
runPlugin('qupath.imagej.detect.cells.PositiveCellDetection', '{"detectionImageBrightfield": "Optical density sum",  "requestedPixelSizeMicrons": 0.5,  "backgroundRadiusMicrons": 8.0,  "medianRadiusMicrons": 0.0,  "sigmaMicrons": 1.5,  "minAreaMicrons": 10.0,  "maxAreaMicrons": 400.0,  "threshold": 0.01,  "maxBackground": 2.0,  "watershedPostProcess": true,  "excludeDAB": false,  "cellExpansionMicrons": 1.5,  "includeNuclei": true,  "smoothBoundaries": true,  "makeMeasurements": true,  "thresholdCompartment": "Nucleus: DAB OD mean",  "thresholdPositive1": 0.3,  "thresholdPositive2": 1.0,  "thresholdPositive3": 1.0,  "singleThreshold": true}');

//Save file with data in txt format
def name = getProjectEntry().getImageName() + '.txt'
path = buildFilePath(path, name)
saveAnnotationMeasurements(path)
print 'Results exported to ' + path