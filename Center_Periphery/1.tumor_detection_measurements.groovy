/*
 * Script for automated tumor tissue detection for VN.
 * 
 * Paper: 
 * @authors:Mara Stoks and Isaac Vieco-Martí
 */

//Set the pixel classifier according to the staining
def pixelClassifier = "Tumor_detection_VN_staining"

//Set the minimum size to be detected (in microns)
def minimumSize = 4000


//Staining
setImageType('BRIGHTFIELD_H_DAB');
setColorDeconvolutionStains('{"Name" : "H-DAB default", "Stain 1" : "Hematoxylin", "Values 1" : "0.65111 0.70119 0.29049", "Stain 2" : "DAB", "Values 2" : "0.26917 0.56824 0.77759", "Background" : " 255 255 255"}');

//Tumor detection
resetSelection();
createAnnotationsFromPixelClassifier(pixelClassifier, minimumSize, 0.0) 
selectAnnotations();

//Dilation and Erosion of the ROI
runPlugin('qupath.lib.plugins.objects.FillAnnotationHolesPlugin', '{}');
runPlugin('qupath.lib.plugins.objects.DilateAnnotationPlugin', '{"radiusMicrons": 100.0,  "removeInterior": false,  "constrainToParent": true}')
clearSelectedObjects(true);
clearSelectedObjects();
selectAnnotations();
runPlugin('qupath.lib.plugins.objects.FillAnnotationHolesPlugin', '{}');
runPlugin('qupath.lib.plugins.objects.DilateAnnotationPlugin', '{"radiusMicrons": -100.0,  "removeInterior": false,  "constrainToParent": true}')
clearSelectedObjects(true);
clearSelectedObjects();

//Add desired measurements. If the annotation is manually edited, re-add measurements
selectAnnotations();
addShapeMeasurements( "SOLIDITY", "MAX_DIAMETER", "MIN_DIAMETER") 