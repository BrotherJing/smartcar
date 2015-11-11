##DataSetVOC

- trainSet: Train.txt��Ӧѵ����ͼƬ���ļ���
- testSet: Test.txt���Լ�ͼƬ���ļ���
- classNames: class.txt�����õ��ģ�20�ָ����������
- gtTrainBoxes, glTestBoxes: vector<vector<Vec4i>>�����ܰ���object��box�ļ��ϡ���ÿ��ͼƬһ��vector<Vec4i>��

### loadAnnotations()
call loadBBoxes() for each img in trainSet and testSet

### loadBBoxes()
open one xml/yml file, call loadBox() for each object in the file
```
FileNode fn = fs["annotation"]["object"];
```

### loadBox()
��ȡobject���������±߽磬����classname��push��box vector����
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
- xTrainP: vector<vector<Mat>>��ÿ��train sample��box��feature���ݶ�ͼ������Щbox�����ǰ���object�ģ����Խ�P(positive)
- xTrainN: vector<vector<Mat>>��������ɵ�box��Ȼ���ȡfeature
ʹ��֮ǰ�����gtTrainBoxes����forѭ���У���ÿ��train sample����ȡͼƬ�������sample��boxes����ȡͼƬ��Ȼ��getFeature()���һ���̶���С���ݶ�ͼ(gradient)��push��xP�С���Щfeature��8*8 float ��Mat
Ȼ���ٰ���Щд���ļ��С�.xP��numP*64��С��ÿһ����һ��feature��.xN��numN*64��С��

###trainStageI()
�ְѸղ�д���ļ��Ķ�������Ȼ��trainSVM()��trainSVM()���棬x��feature��Ϊinput��y��output��1����positive��-1����negative��ͨ�����ѵ����һ��model�����model�Ĺؼ������weight��������������Ǹ�weight�������ת��Ϊfloat���飬���reshape��8*8�����飬д��.wS1���ļ�����

###trainStageII()
��loadTrainModel()
��ÿ��train sample����ͼƬ��predictBBoxSI()

###loadTrainModel()

###predictBBoxSI()
