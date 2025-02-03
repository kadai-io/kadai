package io.kadai.user.internal;

import io.kadai.user.internal.models.UserImpl;
import java.util.List;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

/**
 * This interface provides a Mapper for querying {@linkplain io.kadai.user.api.models.User Users}.
 */
public interface UserQueryMapper {

  @Select(
      "<script>SELECT USER_ID, FIRST_NAME, LASTNAME, FULL_NAME, LONG_NAME, E_MAIL, PHONE, "
          + "MOBILE_PHONE, ORG_LEVEL_4, ORG_LEVEL_3, ORG_LEVEL_2, ORG_LEVEL_1, DATA "
          + "FROM USER_INFO "
          + "<where>"
          + "<if test='idIn != null'>AND USER_ID IN"
          + "(<foreach item='item' collection='idIn' separator=',' >#{item}</foreach>)</if> "
          + "<if test='orgLevel1In != null'>AND ORG_LEVEL_1 IN"
          + "(<foreach item='item' collection='orgLevel1In' separator=',' >#{item}</foreach>)</if> "
          + "<if test='orgLevel2In != null'>AND ORG_LEVEL_2 IN"
          + "(<foreach item='item' collection='orgLevel2In' separator=',' >#{item}</foreach>)</if> "
          + "<if test='orgLevel3In != null'>AND ORG_LEVEL_3 IN"
          + "(<foreach item='item' collection='orgLevel3In' separator=',' >#{item}</foreach>)</if> "
          + "<if test='orgLevel4In != null'>AND ORG_LEVEL_4 IN"
          + "(<foreach item='item' collection='orgLevel4In' separator=',' >#{item}</foreach>)</if> "
          + "</where>"
          + "<if test='!orderBy.isEmpty()'>ORDER BY "
          + "<foreach item='item' collection='orderBy' separator=',' >${item}</foreach></if> "
          + "<if test=\"_databaseId == 'db2'\">with UR </if> "
          + "</script>")
  @Result(property = "id", column = "USER_ID")
  @Result(
      property = "groups",
      column = "USER_ID",
      many = @Many(select = "io.kadai.user.internal.UserMapper.findGroupsById"))
  @Result(
      property = "permissions",
      column = "USER_ID",
      many = @Many(select = "io.kadai.user.internal.UserMapper.findPermissionsById"))
  @Result(property = "firstName", column = "FIRST_NAME")
  @Result(property = "lastName", column = "LASTNAME")
  @Result(property = "fullName", column = "FULL_NAME")
  @Result(property = "longName", column = "LONG_NAME")
  @Result(property = "email", column = "E_MAIL")
  @Result(property = "phone", column = "PHONE")
  @Result(property = "mobilePhone", column = "MOBILE_PHONE")
  @Result(property = "orgLevel4", column = "ORG_LEVEL_4")
  @Result(property = "orgLevel3", column = "ORG_LEVEL_3")
  @Result(property = "orgLevel2", column = "ORG_LEVEL_2")
  @Result(property = "orgLevel1", column = "ORG_LEVEL_1")
  @Result(property = "data", column = "DATA")
  List<UserImpl> queryUsers(UserQueryImpl userQuery);

  @Select(
      "<script>SELECT COUNT(USER_ID) FROM USER_INFO "
          + "<where>"
          + "<if test='idIn != null'>AND USER_ID IN"
          + "(<foreach item='item' collection='idIn' separator=',' >#{item}</foreach>)</if> "
          + "<if test='orgLevel1In != null'>AND ORG_LEVEL_1 IN"
          + "(<foreach item='item' collection='orgLevel1In' separator=',' >#{item}</foreach>)</if> "
          + "<if test='orgLevel2In != null'>AND ORG_LEVEL_2 IN"
          + "(<foreach item='item' collection='orgLevel2In' separator=',' >#{item}</foreach>)</if> "
          + "<if test='orgLevel3In != null'>AND ORG_LEVEL_3 IN"
          + "(<foreach item='item' collection='orgLevel3In' separator=',' >#{item}</foreach>)</if> "
          + "<if test='orgLevel4In != null'>AND ORG_LEVEL_4 IN"
          + "(<foreach item='item' collection='orgLevel4In' separator=',' >#{item}</foreach>)</if> "
          + "</where>"
          + "<if test='!orderBy.isEmpty()'>ORDER BY "
          + "<foreach item='item' collection='orderBy' separator=',' >${item}</foreach></if> "
          + "<if test=\"_databaseId == 'db2'\">with UR </if> "
          + "</script>")
  List<UserImpl> countQueryUsers(UserQueryImpl userQuery);

  @Select(
      "<script>SELECT DISTINCT ${columnName} FROM USER_INFO "
          + "<where>"
          + "<if test='idIn != null'>AND USER_ID IN"
          + "(<foreach item='item' collection='idIn' separator=',' >#{item}</foreach>)</if> "
          + "<if test='orgLevel1In != null'>AND ORG_LEVEL_1 IN"
          + "(<foreach item='item' collection='orgLevel1In' separator=',' >#{item}</foreach>)</if> "
          + "<if test='orgLevel2In != null'>AND ORG_LEVEL_2 IN"
          + "(<foreach item='item' collection='orgLevel2In' separator=',' >#{item}</foreach>)</if> "
          + "<if test='orgLevel3In != null'>AND ORG_LEVEL_3 IN"
          + "(<foreach item='item' collection='orgLevel3In' separator=',' >#{item}</foreach>)</if> "
          + "<if test='orgLevel4In != null'>AND ORG_LEVEL_4 IN"
          + "(<foreach item='item' collection='orgLevel4In' separator=',' >#{item}</foreach>)</if> "
          + "</where>"
          + "<if test='!orderBy.isEmpty()'>ORDER BY "
          + "<foreach item='item' collection='orderBy' separator=',' >${item}</foreach></if> "
          + "<if test=\"_databaseId == 'db2'\">with UR </if> "
          + "</script>")
  List<String> queryUserColumnValues(UserQueryImpl userQuery);
}
