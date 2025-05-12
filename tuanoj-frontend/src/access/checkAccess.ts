/**
 * 检查权限
 * @param loginUser 当前登陆用户
 * @param needAccess 需要有的权限
 * @return boolean 有无权限
 */
import ACCESSENUM from "@/access/accessEnum";

const checkAccess = (loginUser: any, needAccess = ACCESSENUM.NOT_LOGIN) => {
  //获取当前用户的权限(如果没有loginUser，表示未登录)
  const loginUserAccess = loginUser?.userRole ?? ACCESSENUM.NOT_LOGIN;

  //如果页面需要的权限是未登录，则直接返回true
  if (needAccess === ACCESSENUM.NOT_LOGIN) {
    return true;
  }
  //如果页面需要的权限是普通用户，则判断是不是登陆了，未登录返回false
  if (needAccess === ACCESSENUM.USER) {
    if (loginUserAccess === ACCESSENUM.NOT_LOGIN) {
      return false;
    }
  }
  //如果页面需要的权限是管理员，则判断是不是管理员，不是管理员返回false
  if (needAccess === ACCESSENUM.ADMIN) {
    if (loginUserAccess !== ACCESSENUM.ADMIN) {
      return false;
    }
  }
  return true;
};

export default checkAccess;
