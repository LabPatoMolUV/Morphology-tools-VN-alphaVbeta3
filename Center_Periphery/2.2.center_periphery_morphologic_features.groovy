/*
 * Script for the determination of the center and periphery of the samples according to morphologic features. 
 * This script was generated by modifying a script by Pete Bankhead that is available at: https://petebankhead.github.io/qupath/scripts/2018/08/08/three-regions.html
 * If the center and periphery of the samples were differentiated according to their solidity (script 2.1) and did not represent each approximately fifty percent of the tumor area, this script was used.
 *
 * Paper:
 * @authors:Mara Stoks and Isaac Vieco-Martí
 */


//Set same path as in script 2.1.
def wd = " "

//Put desired value. Value was 50 microns for tumors <50 mm2 and 100 microns for tumors >1 mm2
inner= 50 	 

//Import libraries
import org.locationtech.jts.geom.Geometry
import qupath.lib.common.GeneralTools
import qupath.lib.objects.PathObject
import qupath.lib.objects.PathObjects
import qupath.lib.roi.GeometryTools
import qupath.lib.roi.ROIs
import static qupath.lib.gui.scripting.QPEx.*

//Inward dilation
double expandMarginMicrons = inner


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////NOT TO CHANGE FROM NOW ON//////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//TUMOR_GJ: Defines the destination folder for the hydrogel GeoJson file
def pathOutput1 = wd + '/1.geojson_files_tumor'

//TUMOR_DATA: Defines the destination folder of the hydrogel area
def pathOutput2 = wd + '/2.data_area_tumor'

//CENTER: Defines the destination of the center GeoJson
def pathOutput3 = wd + '/3.geojson_files_center'

//PERIPHERY: Defines the destination of the periphery GeoJson
def pathOutput4 = wd + '/4.geojson_files_periphery'

//CENTER_PERIPHERY_DATA: Defines where center and periphery data go
def pathOutput5 = wd + '/5.data_area_center_periphery' 

//Get the main QuPath data structures
def imageData = getCurrentImageData()
def hierarchy = imageData.getHierarchy()
def server = imageData.getServer()
def analyzed_image = server.getMetadata().getName()

print("Image processing begins --> " + analyzed_image)


////////////////////////
//EXPORT TUMOR GEOJSON//
////////////////////////

selectObjectsByClassification("Tumor")
def name1 = getCurrentImageData().getServer().getMetadata().getName()
def path1 = pathOutput1 + "/" + name1 + ".geojson"
def annotations1 = hierarchy.getAnnotationObjects()
exportObjectsToGeoJson(annotations1, path1, "FEATURE_COLLECTION")   
print("GeoJson exported")


/////////////////////////////
//EXPORT AREA DATA OF TUMOR//
/////////////////////////////

def name_hg = getProjectEntry().getImageName() + '.txt'
path_hg = buildFilePath(pathOutput2, name_hg)
saveAnnotationMeasurements(path_hg)
print("Data exported")


//////////////////////////////////////////////////////////////
//////////CENTER-PERIPHERY ACCORDING TO MORPHOLOGY////////////
//////////////////////////////////////////////////////////////

//Define the colors
def coloInnerMargin = getColorRGB(0, 0, 200)
def colorOuterMargin = getColorRGB(0, 200, 0)
def colorCentral = getColorRGB(0, 0, 0)

//Lock annotations
def lockAnnotations = true

//Calculation pixel size
def cal = server.getPixelCalibration()
if (!cal.hasPixelSizeMicrons()) {
  print 'We need the pixel size information here!'
  return
}
if (!GeneralTools.almostTheSame(cal.getPixelWidthMicrons(), cal.getPixelHeightMicrons(), 0.0001)) {
  print 'Warning! The pixel width & height are different; the average of both will be used'
}

//Select annotations
def annotations = getAnnotationObjects()
def selected =  annotations[0]
if (selected == null || !selected.isAnnotation()) {
  print 'Please select an annotation object!'
  return
}

//We need one selected annotation as a starting point; if we have other annotations, they will constrain the output
annotations.remove(selected)

//Extract the ROI & plane
def roiOriginal = selected.getROI()
def plane = roiOriginal.getImagePlane()

//If we have at most one other annotation, it represents the tissue
Geometry areaTissue
PathObject tissueAnnotation
if (annotations.isEmpty()) {
  areaTissue = ROIs.createRectangleROI(0, 0, server.getWidth(), server.getHeight(), plane).getGeometry()
} else if (annotations.size() == 1) {
  tissueAnnotation = annotations.get(0)
  areaTissue = tissueAnnotation.getROI().getGeometry()
} else {
  print 'Sorry, this script only support one selected annotation for the tumor region, and at most one other annotation to constrain the expansion'
  return
}

//Calculation of margin dilation
double expandPixels = expandMarginMicrons / cal.getAveragedPixelSizeMicrons()
def areaTumor = roiOriginal.getGeometry()

//Definition of central area
def geomCentral = areaTumor.buffer(-expandPixels)
geomCentral = geomCentral.intersection(areaTissue)
def roiCentral = GeometryTools.geometryToROI(geomCentral, plane)
def annotationCentral = PathObjects.createAnnotationObject(roiCentral)
annotationCentral.setName("Center")
annotationCentral.setColorRGB(colorCentral)
annotationCentral.each{it.setPathClass(getPathClass("Center"))}

//Definition of inner margin area
def geomInner = areaTumor
geomInner = geomInner.difference(geomCentral)
geomInner = geomInner.intersection(areaTissue)
def roiInner = GeometryTools.geometryToROI(geomInner, plane)
def annotationInner = PathObjects.createAnnotationObject(roiInner)
annotationInner.setName("Inner margin")
annotationInner.setColorRGB(coloInnerMargin)
annotationInner.each{it.setPathClass(getPathClass("Inner margin"))}

//Addition of generated annotations (center and inner margin)
hierarchy.getSelectionModel().clearSelection()
hierarchy.removeObject(selected, true)
def annotationsToAdd = [ annotationInner, annotationCentral];
annotationsToAdd.each {it.setLocked(lockAnnotations)}
if (tissueAnnotation == null) {
  hierarchy.addPathObjects(annotationsToAdd)
} else {
  tissueAnnotation.addPathObjects(annotationsToAdd)
  hierarchy.fireHierarchyChangedEvent(this, tissueAnnotation)
  if (lockAnnotations)
    tissueAnnotation.setLocked(true)
}


///////////////////////////////
/////EXPORT CENTER GEOJSON/////
///////////////////////////////

selectObjectsByClassification("Center")
def annotations3 = getSelectedObjects()
def name3 = getCurrentImageData().getServer().getMetadata().getName()
def path3 = pathOutput3+ "/" + name3 + ".geojson"
exportObjectsToGeoJson(annotations3, path3, "FEATURE_COLLECTION")   
print("Center GeoJson exported")
resetSelection()


///////////////////////////////////
/////EXPORT PERIPHERY GEOJSON/////
//////////////////////////////////

selectObjectsByClassification("Inner margin")
def annotations4 = getSelectedObjects()
def name4 = getCurrentImageData().getServer().getMetadata().getName()
def path4 = pathOutput4+ "/" + name4 + ".geojson"
exportObjectsToGeoJson(annotations4, path4, "FEATURE_COLLECTION")   
print("Periphery GeoJson exported")


////////////////////////////////////
////EXPORT CENTER-PERIPHERY DATA////
////////////////////////////////////

path_centro_periferia = buildFilePath(pathOutput5, name_hg)
saveAnnotationMeasurements(path_centro_periferia)
print("Data of center and periphery exported")