##DataSetVOC

- trainSet: Train.txt对应训练集图片的文件名
- testSet: Test.txt测试集图片的文件名
- classNames: class.txt分类用到的，20种个物体的名字
- gtTrainBoxes, glTestBoxes: vector<vector<Vec4i>>，可能包含object的box的集合。（每张图片一个vector<Vec4i>）

### loadAnnotations()
call loadBBoxes() for each img in trainSet and testSet

### loadBBoxes()
open one xml/yml file, call loadBox() for each object in the file
```
FileNode fn = fs["annotation"]["object"];
```

### loadBox()
获取object的左右上下边界，还有classname，push到box vector里面
```
fn["bndbox"]["xmin"] >> strXmin;
fn["bndbox"]["ymin"] >> strYmin;
fn["bndbox"]["xmax"] >> strXmax;
fn["bndbox"]["ymax"] >> strYmax;

fn["name"]>>clsName;
```

## Objectness
_W:  size of feature window?

###trainObjectness()
- generateTrainData()
- trainStageI()
- trainStageII()

###generateTrainData()
- xTrainP: vector<vector<Mat>>，每个train sample的box的feature（梯度图），这些box里面是包含object的，所以叫P(positive)
- xTrainN: vector<vector<Mat>>，随机生成的box，然后获取feature
使用之前载入的gtTrainBoxes，在for循环中，对每个train sample，读取图片，用这个sample的boxes来截取图片，然后getFeature()获得一个固定大小的梯度图(gradient)，push到xP中。这些feature是8*8 float 的Mat
然后再把这些写到文件中。.xP是numP*64大小，每一行是一个feature，.xN是numN*64大小。

###trainStageI()
又把刚才写到文件的读进来。然后trainSVM()。trainSVM()里面，x是feature作为input，y是output，1代表positive，-1代表negative。通过这个训练出一个model。这个model的关键里面的weight，就是神经网络的那个weight。把这个转化为float数组，最后reshape成8*8的数组，写到.wS1的文件里面

###trainStageII()
先loadTrainModel()
对每个train sample，打开图片，predictBBoxSI()

###loadTrainModel()

###predictBBoxSI()
