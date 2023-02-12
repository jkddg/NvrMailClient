package com.jkddg.nvrmailclient.opencv;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * @Author 黄永好
 * @create 2023/2/6 16:46
 * 人体识别
 */

@Slf4j
public class HumanBodyRecognition {
    public static Mat findPeople(Mat srcImage) {

        /*
         * IMREAD_UNCHANGED = -1 ：不进行转化，比如保存为了16位的图片，读取出来仍然为16位。
         * IMREAD_GRAYSCALE = 0 ：进行转化为灰度图，比如保存为了16位的图片，读取出来为8位，类型为CV_8UC1。
         * IMREAD_COLOR = 1 ：进行转化为三通道图像。
         * IMREAD_ANYDEPTH = 2 ：如果图像深度为16位则读出为16位，32位则读出为32位，其余的转化为8位。
         * IMREAD_ANYCOLOR = 4 ：图像以任何可能的颜色格式读取
         * IMREAD_LOAD_GDAL = 8 ：使用GDAL驱动读取文件，GDAL(Geospatial Data Abstraction
         * Library)是一个在X/MIT许可协议下的开源栅格空间数据转换库。它利用抽象数据模型来表达所支持的各种文件格式。
         *	它还有一系列命令行工具来进行数据转换和处理。
         */

        Mat gary = new Mat();
        //图片转灰 https://blog.csdn.net/ren365880/article/details/103869207
        /*	3. 图片转为单通道
         *  将图像从一种颜色空间转换为另一种颜色空间。
         *	该功能将输入图像从一种颜色空间转换为另一种颜色空间。
         *	在从RGB颜色空间转换的情况下，应明确指定通道的顺序（RGB或BGR）。
         *	请注意，OpenCV中的默认颜色格式通常称为RGB，但实际上是BGR（字节是相反的）。因此，
         *	标准（24位）彩色图像中的第一个字节将是8位蓝色分量，第二个字节将是绿色分量，第三个字节将是红色分量。
         *	第四，第五和第六个字节将是第二个像素（蓝色，然后是绿色，然后是红色），依此类推。
         *	R，G和B通道值的常规范围是：
         *	CV_8U图像为0至255
         *	CV_16U图像为0至65535
         *	CV_32F图像为0到1
         *	在线性变换的情况下，范围无关紧要。
         *	但是在进行非线性转换的情况下，应将输入的RGB图像规范化为适当的值范围，以获得正确的结果，
         *	例如，对于RGB到LUV（LUV色彩空间全称CIE 1976(L*,u*,v*) （也作CIELUV）色彩空间，L*表示物体亮度，
         *	u*和v*是色度。于1976年由国际照明委员会CIE 提出，由CIE XYZ空间经简单变换得到，具视觉统一性。类似的色
         *	彩空间有CIELAB。对于一般的图像，u*和v*的取值范围为-100到+100，亮度为0到100。）转换。
         *	例如，如果您有一个32位浮点图像直接从8位图像转换而没有任何缩放，则它将具有0..255的值范围，
         *	而不是该函数假定的0..1。因此，在调用#cvtColor之前，您需要先按比例缩小图像：
         *	img = 1./255;
         *	cvtColor（img，img，COLOR_BGR2Luv）;
         *	如果将#cvtColor与8位图像一起使用，转换将丢失一些信息。对于许多应用程序来说，这不会引起注意，
         *	但是建议在需要全部颜色范围的应用程序中使用32位图像，或者在进行操作之前先转换图像然后再转换回来。
         *	如果转换添加了Alpha通道，则其值将设置为相应通道范围的最大值：CV_8U为255，CV_16U为65535，CV_32F为1。
         *	@param src输入图像：8位无符号，16位无符号（CV_16UC ...）或单精度浮点数。
         *	@param dst输出图像的大小和深度与src相同。
         *	@param代码颜色空间转换代码。
         *	通道是自动从src和代码派生的。
         */
        /*
         * 下面附上第三个参数的详细解释
         */
        //使用灰度图像加快检测速度
        Imgproc.cvtColor(srcImage, gary, Imgproc.COLOR_BGR2GRAY);
        /*
         * 使用默认参数创建HOG检测器。
         * 默认值（Size（64,128），Size（16,16），Size（8,8），Size（8,8），9）
         * new Size(64,128),new Size(16,16), new Size(8,8),new Size(8,8),9
         */
        HOGDescriptor hog = new HOGDescriptor();
        /*
         * 设置线性SVM分类器的系数。 线性SVM分类器的
         * @param svmdetector系数。
         * HOGDescriptor.getDefaultPeopleDetector()返回经过训练可进行人员检测的分类器的系数（对于64x128窗口）。
         */
        hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());

        MatOfRect rect = new MatOfRect();
        /*
         * 检测输入图像中不同大小的对象。 检测到的对象将作为矩形列表返回。
         * @param img类型CV_8U或CV_8UC3的矩阵，其中包含检测到对象的图像。
         * @param foundLocations矩形的向量，其中每个矩形都包含检测到的对象。
         * @param foundWeights向量，它将包含每个检测到的对象的置信度值。
         * @param hitThreshold要素与SVM分类平面之间距离的阈值，通常为0，应在检测器系数中指定
         *  （作为最后一个自由系数），但是如果省略自由系数（允许），则可以指定 在这里手动操作。
         * @param winStride窗口跨度。 它必须是跨步的倍数。
         * @param padding填充
         */
        hog.detectMultiScale(gary, rect, new MatOfDouble(), 0.27, new Size(8, 8), new Size(8, 8), 1.09);

        Rect[] rects = rect.toArray();
        boolean found = false;
        for (int i = 0; i < rects.length; i++) {
            found = true;
            /*
             * 绘制一个简单的，粗的或实心的直角矩形。 函数cv :: rectangle绘制一个矩形轮廓或一个填充的矩形，其两个相对角为pt1和pt2。
             * @param img图片。
             * @param pt1矩形的顶点。
             * @param pt2与pt1相反的矩形的顶点。
             * @param color矩形的颜色或亮度（灰度图像）。
             * @param thickness组成矩形的线的粗细。 负值（如#FILLED）表示该函数必须绘制一个填充的矩形。
             * @param lineType线的类型。 请参阅https://blog.csdn.net/ren365880/article/details/103952856
             */
//            Rect matchRect = shrinkPanel(rects[i]);
            Imgproc.rectangle(srcImage, new Point(rects[i].x, rects[i].y), new Point(rects[i].x + rects[i].width, rects[i].y + rects[i].height), new Scalar(0, 255, 0), 2, Imgproc.LINE_AA);
        }
        if (found) {
            return srcImage;
        }
        return null;
    }

//    /**
//     * 原来的框太大，缩小一下
//     *
//     * @param rect
//     * @return
//     */
//    private static Rect shrinkPanel(Rect rect) {
//        rect.x = rect.x + (int) (rect.x * 0.01);
//        rect.y = rect.y + (int) (rect.y * 0.25);
//        rect.width = rect.width - (int) (rect.width * 0.2);
//        rect.height = rect.height - (int) (rect.height * 0.2);
//        return rect;
//    }

    public static Mat getImageMat(String path) {
        Mat mat = Imgcodecs.imread(path);
        return mat;
    }

    public static Mat getImageMat(byte[] image) {
        Mat mat = Imgcodecs.imdecode(new MatOfByte(image), Imgcodecs.IMREAD_COLOR);
        return mat;
    }

    public static void writeImage(Mat image) {
        Imgcodecs.imwrite("D:\\human\\20230206095459.jpg", image);
    }

    public static void test() {
        String[] path = new String[]{"前门-20230211175819.jpg","内院-20230211173923.jpg","前门-20230211173509.jpg","前门-20230211171659.jpg","前门-20230211165342.jpg","前门-20230211165606.jpg","内院-20230211105657-1.jpg", "内院-20230211103714-1.jpg", "20230206095459-1.jpg", "2020011813492339.jpg", "内院-20230210123330-1.jpg", "前门-20230211101215-1.jpg", "前门-20230211100603-1.jpg", "内院-20230211102133-1.jpg", "内院-20230211102643-1.jpg"};
        for (String s : path) {
            File file = new File("D:\\human\\" + s);
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[(int) file.length()];
                fileInputStream.read(buffer);
                fileInputStream.close();
                buffer = findPeople(buffer);
                File file1 = new File("D:\\human\\结果\\结果" + s);
                if (file1.exists()) {
                    file1.delete();
                }
                if (buffer != null) {
                    FileOutputStream fileOutputStream = new FileOutputStream(file1);
                    fileOutputStream.write(buffer);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        }

    }

    public static byte[] findPeople(byte[] srcImage) {
        long startTime = System.currentTimeMillis();
        Mat mat = getImageMat(srcImage);
        try {
            mat = findPeople(mat);
        } catch (Exception ex) {
            log.error("opencv异常" + ex.getMessage() + ex.getStackTrace());
        }
        long consumeTime = System.currentTimeMillis() - startTime;
        log.info("findPeople耗时:" + consumeTime + "毫秒");
        if (mat == null) {
            return null;
        }
        MatOfByte matOfByte = new MatOfByte();
        MatOfInt moi = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 60);
        Imgcodecs.imencode(".jpg", mat, matOfByte, moi);
        return matOfByte.toArray();
    }

}
