package gradle.demo.dao;

import gradle.demo.base.BaseMapperTemplate;
import gradle.demo.model.CourseUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author JingQ on 2017/12/24.
 */
@Mapper
public interface CourseUserMapper extends BaseMapperTemplate<CourseUser> {

    /**
     * 根据用户ID查询选修的课
     *
     * @param userId 用户id
     * @return 课程和用户的关系
     */
    List<CourseUser> selectByUserId(@Param("userId") Integer userId);

    /**
     * 查询选修courseId的userId
     *
     * @param courseId 课程ID
     * @return userId列表
     */
    List<Integer> selectUserIdsByCId(@Param("courseId") Integer courseId);

    /**
     * 根据课程ID进行查询用户总数
     *
     * @param courseId 课程ID
     * @return 用户总数
     */
    int selectCountByCId(@Param("courseId") Integer courseId);

    /**
     * 查出该课程对应的用户列表
     *
     * @param courseId
     * @return
     */
    List<CourseUser> selectByCourseId(@Param("courseId") Integer courseId);

    /**
     * 查询
     * @param courseId
     * @param userId
     * @return
     */
    CourseUser selectByCourseIdAndUserId(@Param("courseId") Integer courseId, @Param("userId") Integer userId);
}
