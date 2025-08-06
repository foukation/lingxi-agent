package com.fxzs.lingxiagent.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.Toast;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文档生成工具类
 * 功能：生成TXT、PDF和Word文档
 */
public class DocumentHelper {

    private Context context;
    private OnDocumentGeneratedListener listener;

    public DocumentHelper(Context context) {
        this.context = context;
    }

    public interface OnDocumentGeneratedListener {
        void onSuccess(String filePath);

        void onFailure(String errorMessage);
    }

    public void setOnDocumentGeneratedListener(OnDocumentGeneratedListener listener) {
        this.listener = listener;
    }

    /**
     * 生成文本文件(.txt)
     * @param text 要保存的文本内容
     * @param fileName 文件名（不带扩展名）
     */
    public void generateTextFile(String text, String fileName) {
        new Thread(() -> {
            try {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                File appDir = new File(dir, context.getPackageName());
                if (!appDir.exists() && !appDir.mkdirs()) {
                    Toast.makeText(context, "无法创建目录", Toast.LENGTH_SHORT);
                    return;
                }
                File file = new File(appDir, fileName + ".txt");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(text.getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 创建包含文本和图像的PDF文件
     *
     * @param text    要添加到PDF中的文本
     * @param image   要添加到PDF中的图像，如果不需要添加图像，则可以传入null
     * @param fileName   生成的PDF文件名，不包括扩展名
     */
    public void createPdfWithTextAndImage(String text, Bitmap image, String fileName) {
        new Thread(() -> {
            try {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                File appDir = new File(dir, context.getPackageName());
                if (!appDir.exists() && !appDir.mkdirs()) {
                    Toast.makeText(context, "无法创建目录", Toast.LENGTH_SHORT);
                    return;
                }
                File file = new File(appDir, fileName + ".pdf");
                PdfWriter writer = new PdfWriter(file);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
                document.add(new Paragraph(text).setFont(font));

                if (image != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    ImageData imageData = ImageDataFactory.create(stream.toByteArray());
                    Image pdfImage = new Image(imageData);
                    pdfImage.setAutoScale(true);
                    document.add(pdfImage);
                }
                document.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 创建包含文本和图片的Word文档(.docx)
     *
     * @param text     要添加到Word中的文本
     * @param bitmap    要添加到Word中的图片(Bitmap)，可为null
     * @param fileName 生成的Word文件名，不包括扩展名
     */
    public void createWordWithTextAndImage(String text, Bitmap bitmap, String fileName) {
        new Thread(() -> {
            XWPFDocument document = null;
            FileOutputStream fos = null;

            try {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                File appDir = new File(dir, context.getPackageName());

                if (!appDir.exists() && !appDir.mkdirs()) {
                    Toast.makeText(context, "无法创建目录", Toast.LENGTH_SHORT);
                    return;
                }

                File file = new File(appDir, fileName + ".docx");
                fos = new FileOutputStream(file);

                document = new XWPFDocument();
                if (text != null && !text.isEmpty()) {
                    addTextToDocument(document, text);
                }
                if (bitmap != null) {
                    addImageToDocument(document, bitmap);
                }
                document.write(fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (document != null) document.close();
                    if (fos != null) fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void addTextToDocument(XWPFDocument document, String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();

            try {
                run.setText(line);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addImageToDocument(XWPFDocument document, Bitmap bitmap) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);

        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();

        run.addPicture(
                new ByteArrayInputStream(bos.toByteArray()),
                XWPFDocument.PICTURE_TYPE_PNG,
                "image.png",
                Units.toEMU(bitmap.getWidth()),
                Units.toEMU(bitmap.getHeight())
        );
    }

}