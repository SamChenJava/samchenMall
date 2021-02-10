package com.samchenjava.mall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.samchenjava.common.utils.HttpUtils;
import com.samchenjava.mall.member.dao.MemberLevelDao;
import com.samchenjava.mall.member.entity.MemberLevelEntity;
import com.samchenjava.mall.member.exception.PhoneNumExistException;
import com.samchenjava.mall.member.exception.UsernameExistException;
import com.samchenjava.mall.member.service.MemberService;
import com.samchenjava.mall.member.vo.SocialUserVo;
import com.samchenjava.mall.member.vo.UserLoginVo;
import com.samchenjava.mall.member.vo.UserRegisterVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.samchenjava.common.utils.PageUtils;
import com.samchenjava.common.utils.Query;

import com.samchenjava.mall.member.dao.MemberDao;
import com.samchenjava.mall.member.entity.MemberEntity;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override//n.216
    public void register(UserRegisterVo vo) {
        MemberDao dao = this.baseMapper;
        MemberEntity memberEntity = new MemberEntity();

        //setting default member level
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());
        //checking whether username and phone number is unique
        //in order to let controller aware exception
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUsername());

        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUsername());
        memberEntity.setNickname(vo.getUsername());
        //password need to be encrypt
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        dao.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneNumExistException {
        MemberDao memberDao = this.baseMapper;
        Integer mobile = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile > 0) {
            throw new PhoneNumExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        MemberDao memberDao = this.baseMapper;
        Integer userName = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (userName > 0) {
            throw new UsernameExistException();
        }
    }

    @Override//n.219
    public MemberEntity login(UserLoginVo vo) {
        String loginAcct = vo.getLoginAcct();
        String rawPassword = vo.getPassword();
        MemberDao baseMapper = this.baseMapper;
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginAcct).or().eq("mobile", loginAcct));
        if (memberEntity == null) {
            return null;
        } else {
            String password = memberEntity.getPassword();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(rawPassword, password);
            if (matches) {
                return memberEntity;
            } else {
                return null;
            }
        }
    }

    @Override//n.223
    public MemberEntity login(SocialUserVo vo) throws Exception {
        String uid = vo.getUid();
        MemberDao dao = this.baseMapper;
        MemberEntity memberEntity = dao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity != null) {
            //this user has already registered
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(vo.getAccess_token());
            update.setExpiresIn(vo.getExpires_in() + "");
            dao.updateById(update);
            memberEntity.setExpiresIn(vo.getExpires_in() + "");
            memberEntity.setAccessToken(vo.getAccess_token());
            return memberEntity;
        }else{
            MemberEntity register = new MemberEntity();
            try{
                Map<String, String> map = new HashMap<>();
                map.put("access_token", vo.getAccess_token());
                map.put("uid", vo.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weio.com", "2/users/show.json", "get", new HashMap<>(), map);
                if(response.getStatusLine().getStatusCode()==200){
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    register.setNickname(name);
                    register.setGender("m".equals(gender)?1:0);
                }
            }catch (Exception e){}

            register.setSocialUid(vo.getUid());
            register.setAccessToken(vo.getAccess_token());
            register.setExpiresIn(vo.getExpires_in() + "");
            dao.insert(register);
            return register;
        }
    }

}