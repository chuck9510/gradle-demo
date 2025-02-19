package gradle.demo.service.impl;

import gradle.demo.dao.HomeworkMapper;
import gradle.demo.model.CourseRecord;
import gradle.demo.model.Homework;
import gradle.demo.model.HomeworkOuter;
import gradle.demo.service.CourseRecordService;
import gradle.demo.service.FileManageService;
import gradle.demo.service.HomeworkOuterService;
import gradle.demo.service.HomeworkService;
import gradle.demo.util.file.UploadObject;
import gradle.demo.util.result.BusinessException;
import gradle.demo.util.result.ExceptionDefinitions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 *
 * @author JingQ on 2017/12/24.
 */
@Slf4j
@Service
public class HomeworkServiceImpl implements HomeworkService {

    private static final String MARK_HOMEWORK_DIR = "mark_homework/";

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Autowired
    private HomeworkOuterService homeworkOuterServiceImpl;

    @Autowired
    private FileManageService fileManageServiceImpl;

    @Override
    public Homework getById(Integer id) {
        return homeworkMapper.selectByPrimaryKey(id);
    }

    @Override
    public int update(Homework record) {
        return homeworkMapper.updateByPrimaryKey(record);
    }

    @Override
    public int deleteById(Integer id) {
        return homeworkMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Homework add(Homework record) {
        homeworkMapper.insert(record);
        return record;
    }

    @Override
    public List<Homework> getDetailsByHomeworkOuterId(Integer homeworkOuterId, Integer userId) {
        if (userId == null) {
            return homeworkMapper.selectByHWOuterId(homeworkOuterId);
        }
        return homeworkMapper.selectByHWOuterIdAndUserId(homeworkOuterId, userId);
    }

    @Override
    public List<String> getHomeworkUrlsByHWOuterID(Integer homeworkOuterId) {
        return homeworkMapper.selectHomeworkUrlsByHWOuterId(homeworkOuterId);
    }

    @Override
    public int markHomework(Integer id, String score, String comment) {
        return homeworkMapper.markHomework(id, score, comment);
    }

    @Override
    public int markHomeworkUrl(Integer id, MultipartFile file) {
        Homework homework = homeworkMapper.selectByPrimaryKey(id);
        if (homework == null) {
            throw new BusinessException(ExceptionDefinitions.HOMEWORK_RECORD_NOT_EXIST);
        }
        String dirPath = MARK_HOMEWORK_DIR + homework.getHomeworkName() + "_" + homework.getCreateDate() + "/";
        String url = fileManageServiceImpl.upload(file, dirPath);
        return homeworkMapper.markHomeworkUrl(id, url);
    }

    @Override
    public int markHomework(MultipartFile file, Integer homeworkOuterId) throws IOException {
        HomeworkOuter homeworkOuter = homeworkOuterServiceImpl.getById(homeworkOuterId);
        String dirPath = MARK_HOMEWORK_DIR + homeworkOuter.getHomeworkName() + "_" + homeworkOuter.getCreateDate() + "/";
        File tmpFile = new File("/usr/local/logs/" + file.getOriginalFilename());
        if (!tmpFile.exists()) {
            tmpFile.createNewFile();
        }
        file.transferTo(tmpFile);
        ZipFile zipFile = new ZipFile(tmpFile);
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(tmpFile));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null && !entry.isDirectory()) {
                String fileName = entry.getName();
                String idNumber = fileName.substring(0, fileName.indexOf("_"));
                InputStream inputStream = zipFile.getInputStream(entry);
                UploadObject uploadObject = new UploadObject();
                uploadObject.setDir(dirPath);
                uploadObject.setIs(inputStream);
                uploadObject.setFullName(fileName+"_批改");
                String url = fileManageServiceImpl.upload(uploadObject);
                homeworkMapper.updateMarkHomework(idNumber, homeworkOuterId, url);
            }
            zis.closeEntry();
            log.info("作业解压成功，批改完成: " + homeworkOuter.getId());
        } catch (IOException e) {
            log.error("作业解压失败", e);
            throw new RuntimeException(e);
        } finally {
            try {
                assert zis != null;
                zis.close();
                //删除tmp文件
                if (tmpFile.exists()) {
                    tmpFile.delete();
                }
            } catch (IOException ex) {
                log.error("作业输入流关闭失败", ex);
        }
    }
        return 0;
    }



}
