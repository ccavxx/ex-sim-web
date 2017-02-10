/*
 * �������� 2005-8-30
 *
 * TODO Ҫ��Ĵ���ɵ��ļ���ģ�壬��ת��
 * ���� �� ��ѡ�� �� Java �� ������ʽ �� ����ģ��
 */
package com.topsec.tsm.sim.auth.form;

public class LoginForm
{ 
    public final String getLoginName()
    {
        return loginName;
    }
    public final void setLoginName(String loginName)
    {
        this.loginName = loginName;
    }
    public final String getPassword()
    {
        return password;
    }
    public final void setPassword(String password)
    {
        this.password = password;
    }

    private String loginName;
    private String password;
}
