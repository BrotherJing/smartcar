## SampleApplicationSession

### fields

- SampleApplicationControl mSessionControl
- int mCamera
- Matrix44F mProjectionMatrix

### methods

- public void initAR
	- execute InitVuforiaTask()

- public void startAR()
	- init and start camera
	- setProjectionMatrix()
	- mSessionControl.doStartTrackers()
	
### inner class

- InitVuforiaTask
	- Vuforia.setInitParameters()
		- put app key here
	- Vuforia.init()
	- mSessionControl.doInitTrackers()
	- execute LoadTrackerTask()

- LoadTrackerTask
	- mSessionControl.doLoadTrackersData()
	
## ImageTargets(Activity)

### fields

- DataSet mCurrentDataset
- SampleApplicationGLView mGlView
- ImageTargetRenderer mRenderer

### onInitARDone()

- initApplicationAR()
- addContentView(mGlView,...)
	- NO setContentView() in onCreate!
- vuforiaAppSession.startAR()

### initApplicationAR()

- mGlView.setRenderer(mRenderer)

## ImageTargetRenderer

### fields

- Vector<Texture> mTextures
- Teapot mTeapot (3D models)

### methods

- private void initRendering()
	- mTeapot = new Teapot()
	- new SampleApplication3DModel().loadModel(".../Buildings.txt")

- private void renderFrame()
	- for i in state.getNumTrackableResults()...
		- for each trackable result, draw 3D model
		
# Data structure

## 3D model(vertices,norms,texture)

| structure		        |
| --------------------- |
| vertex_count:int      |
| vertex_coords:float   |
| norm_count:int        |
| norm_vectors:float    |
| text_count:int        |
| norm_coords:float     |

