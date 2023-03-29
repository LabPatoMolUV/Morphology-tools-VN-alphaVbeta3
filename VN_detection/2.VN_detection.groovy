/* 
 * Script used for vitronectin detection in the previously generated ROIs
 * 
 * If needed change the classifiers.
 * 
 * Paper:
 *@authors:Mara Stoks and Isaac Vieco-Martí
 */

//Set path of interest to save the data
def path = "   " 


//Pixel classifiers used for tVN and interVN detection
def tumorDetection = "Tumor_detection_VN"

def totalVN = "Total_VN_detection"

def splitVN = "Territorial_and_interterritorial_distinction"

def tVN = "Territorial_VN_detection"

def interVN = "Interterritorial_VN_detection"


//Staining
setImageType('BRIGHTFIELD_H_DAB');
setColorDeconvolutionStains('{"Name" : "H-DAB default", "Stain 1" : "Hematoxylin", "Values 1" : "0.65111 0.70119 0.29049", "Stain 2" : "DAB", "Values 2" : "0.26917 0.56824 0.77759", "Background" : " 255 255 255"}');

//Tumor detection
resetSelection();
selectAnnotations();
createAnnotationsFromPixelClassifier(tumorDetection , 0.0, 0.0) 

//General VN detection
selectObjectsByClassification("Tumor");
createAnnotationsFromPixelClassifier(totalVN , 0.0, 0.0) 
selectObjectsByClassification("Negative");
clearSelectedObjects(true);
clearSelectedObjects();

//Territorial and interterritorial VN distinction
selectObjectsByClassification("Positive");
createAnnotationsFromPixelClassifier(splitVN, 0.0, 0.0) 
selectObjectsByClassification("Positive");
clearSelectedObjects(true);
clearSelectedObjects();

//Territorial VN detection
selectObjectsByClassification("Initial_territorialVN");
createAnnotationsFromPixelClassifier(tVN, 0.0, 0.0) 
selectObjectsByClassification("Initial_territorialVN");
clearSelectedObjects(true);
clearSelectedObjects();

//Interterritorial VN detection
selectObjectsByClassification("Initial_interterritorialVN");
createAnnotationsFromPixelClassifier(interVN, 0.0, 0.0) 
selectObjectsByClassification("Initial_interterritorialVN");
clearSelectedObjects(true);
clearSelectedObjects()

//Save file with data in txt format
def name = getProjectEntry().getImageName() + '.txt'
path = buildFilePath(path, name)
saveAnnotationMeasurements(path)
print 'Results exported to ' + path