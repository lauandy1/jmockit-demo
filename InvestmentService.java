/*
* Copyright (c) 2015. 金联所. All rights reserved.
*
* The copyright to the computer software herein is the property of 金联所.
* The software may be used and/or copied only with the written permission
* of 金联所, or in accordance with the terms and conditions stipulated in
* the agreement/contract under which the software has been supplied.
*
*/
package com.unfae.octopus.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import com.ippjr.calendar.IPPJRCalendar;
import com.ippjr.common.calculator.CalculationInput;
import com.ippjr.common.calculator.CalculationOutput;
import com.ippjr.common.calculator.Calculator;
import com.ippjr.common.calculator.PeriodType;
import com.ippjr.common.calculator.RepaymentType;
import com.ippjr.utils.encrypt.Digest;
import com.unfae.octopus.OctopusException;
import com.unfae.octopus.constant.OctopusError;
import com.unfae.octopus.dao.BorrowerBorrowRecordDao;
import com.unfae.octopus.dao.BorrowerRefundPlanDao;
import com.unfae.octopus.dao.CouponDao;
import com.unfae.octopus.dao.CouponRuleDao;
import com.unfae.octopus.dao.FundDelayDao;
import com.unfae.octopus.dao.FundRecordDao;
import com.unfae.octopus.dao.MemberAccountDao;
import com.unfae.octopus.dao.MemberAccountSnapshotDao;
import com.unfae.octopus.dao.MemberCouponDao;
import com.unfae.octopus.dao.MemberDao;
import com.unfae.octopus.dao.MemberFundRecordDao;
import com.unfae.octopus.dao.MemberInvestmentDao;
import com.unfae.octopus.dao.PlatformAccountDao;
import com.unfae.octopus.dao.PlatformOperationLogDao;
import com.unfae.octopus.dao.ProductCategorySettingDao;
import com.unfae.octopus.dao.ProductDao;
import com.unfae.octopus.dao.ProductItemDao;
import com.unfae.octopus.dao.ProductItemInvestInfoDao;
import com.unfae.octopus.dao.ProductItemSettingDao;
import com.unfae.octopus.dao.ProductItemTransferInfoDao;
import com.unfae.octopus.dao.ReceiptPlanDao;
import com.unfae.octopus.dao.XpressWhitelistDao;
import com.unfae.octopus.dao.condition.MemberAccountCondition;
import com.unfae.octopus.dao.condition.MemberFundRecordCondition;
import com.unfae.octopus.entity.BorrowerBorrowRecord;
import com.unfae.octopus.entity.BorrowerRefundPlan;
import com.unfae.octopus.entity.Coupon;
import com.unfae.octopus.entity.CouponRule;
import com.unfae.octopus.entity.FundDelay;
import com.unfae.octopus.entity.Member;
import com.unfae.octopus.entity.MemberAccount;
import com.unfae.octopus.entity.MemberAccountSnapshot;
import com.unfae.octopus.entity.MemberCoupon;
import com.unfae.octopus.entity.MemberFundRecord;
import com.unfae.octopus.entity.MemberInvestment;
import com.unfae.octopus.entity.MemberReceiptPlan;
import com.unfae.octopus.entity.PlatformOperationLog;
import com.unfae.octopus.entity.PlatformOperationLog.PlatformFundStatus;
import com.unfae.octopus.entity.PlatformOperationLog.PlatformFundType;
import com.unfae.octopus.entity.Product;
import com.unfae.octopus.entity.ProductCategorySetting;
import com.unfae.octopus.entity.ProductItem;
import com.unfae.octopus.entity.ProductItemInvestInfo;
import com.unfae.octopus.entity.ProductItemSetting;
import com.unfae.octopus.entity.XpressDedemptionIn;
import com.unfae.octopus.entity.XpressWhitelist;
import com.unfae.octopus.enums.AccountAmountType;
import com.unfae.octopus.enums.AccountType;
import com.unfae.octopus.enums.BorrowRecordStatus;
import com.unfae.octopus.enums.CouponStatus;
import com.unfae.octopus.enums.FundDelayStatus;
import com.unfae.octopus.enums.FundRecordCategory;
import com.unfae.octopus.enums.InterestStartMode;
import com.unfae.octopus.enums.InvestMode;
import com.unfae.octopus.enums.InvestmentStatus;
import com.unfae.octopus.enums.MemberCouponStatus;
import com.unfae.octopus.enums.MemberType;
import com.unfae.octopus.enums.OctopusProperty;
import com.unfae.octopus.enums.ProductDictProperty;
import com.unfae.octopus.enums.ProductStatus;
import com.unfae.octopus.enums.ProductType;
import com.unfae.octopus.enums.ReceiptPlanStatus;
import com.unfae.octopus.enums.RefundPlanStatus;
import com.unfae.octopus.enums.RevokeType;
import com.unfae.octopus.enums.TermType;
import com.unfae.octopus.enums.Type;
import com.unfae.octopus.util.CustomConverter;
import com.unfae.octopus.util.DateUtils;
import com.unfae.octopus.vo.InvestResult;
import com.unfae.octopus.vo.InvestmentRecordVo;
import com.unfae.octopus.vo.InvestmentStatusVo;
import com.unfae.octopus.vo.InvestmentTotalVo;
import com.unfae.octopus.vo.MemberCouponVo;
import com.unfae.octopus.vo.MemberInvestmentsOverview;
import com.unfae.octopus.vo.Pagination;
import com.unfae.octopus.vo.ReceiptsPlan;
import com.unfae.octopus.vo.RevokedVo;

/**
* 投资REST API 实现
*
* @author ZhangLiang 2015年08月14日
*/
@Service
public class InvestmentService extends AbstractService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InvestmentService.class);
    @Resource
    private MemberInvestmentDao memberInvestmentDao;
    @Resource
    private MemberDao memberDao;
    @Resource
    private ProductItemDao productItemDao;
    @Resource
    private ProductDao productDao;
    @Resource
    private ProductItemSettingDao productItemSettingDao;
    @Resource
    private ProductItemInvestInfoDao productItemInvestInfoDao;
    @Resource
    private MemberAccountDao memberAccountDao;
    @Resource
    private FundRecordDao fundRecordDao;
    @Resource
    private FundDelayDao fundDelayDao;
    @Resource
    private PlatformAccountDao platformAccountDao;
    @Resource
    private ReceiptPlanDao receiptPlanDao;
    @Resource
    private BorrowerBorrowRecordDao borrowerBorrowRecordDao;
    @Resource
    private BorrowerRefundPlanDao borrowerRefundPlanDao;
    @Resource
    private XpressWhitelistDao xpressWhitelistDao;
    @Resource
    private ProductCategorySettingDao productCategorySettingDao;
    @Resource
    private ProductItemTransferInfoDao productItemTransferInfoDao;
    @Resource
    private MemberFundRecordDao memberFundRecordDao;
    @Resource 
    private MemberAccountSnapshotDao memberAccountSnapshotDao;
    @Resource
    private PlatformOperationLogDao platformOperationLogDao;
    @Resource
    private MemberCouponDao memberCouponDao;
    @Resource
    private CouponDao couponDao;
    @Resource
    private CouponRuleDao couponRuleDao;

    /**
     * 查询会员投资信息概览
     *
     * @param memberId 会员ID
     * @return 会员投资信息概览
     */
    public Map<String, MemberInvestmentsOverview> getMemberInvestmentsOverview(String memberId) {
        List<MemberInvestmentsOverview> investmentsOverviews = memberInvestmentDao.getMemberInvestmentsOverview(memberId);
        Map<String, MemberInvestmentsOverview> overviewMap = new HashMap<>();
        if (null != investmentsOverviews && 0 < investmentsOverviews.size()) {
            for (MemberInvestmentsOverview investmentsOverview : investmentsOverviews) {
                investmentsOverview.setAverageRate(investmentsOverview.getAverageRate().setScale(4, BigDecimal.ROUND_DOWN));
                investmentsOverview.setTotalAmount(investmentsOverview.getTotalAmount().setScale(2, BigDecimal.ROUND_DOWN));
                overviewMap.put(investmentsOverview.getType(), investmentsOverview);
            }
        }
        return overviewMap;
    }

    /**
     * 投资指定产品
     *
     * @param params memberId 用户ID
     *               productItemId 产品项ID
     *               amount 投资金额
     *               tradePassword 交易密码MD5
     *               investMode 投资方式
     * @return 投资结果
     * @throws OctopusException
     */
    @Transactional
    public InvestResult invest(MultiValueMap<String, String> params) {
        String memberId = params.getFirst(OctopusProperty.memberId.name());
        String productItemId = params.getFirst(OctopusProperty.productUuid.name());
        BigDecimal amount = new BigDecimal(params.getFirst(OctopusProperty.amount.name()));
        String tradePassword = params.getFirst(OctopusProperty.tradePassword.name());
        String investMode = params.getFirst(OctopusProperty.investMode.name());
        Long memberCouponId = null;
        if (params.getFirst(OctopusProperty.couponId.name()) != null) {
            memberCouponId = Long.parseLong(params.getFirst(OctopusProperty.couponId.name()));
        }
        Integer clientType = Integer.parseInt(params.getFirst(OctopusProperty.clientType.name()));
        InvestData investData = prepareAndValidInvestData(memberId, productItemId, amount, tradePassword, investMode, memberCouponId);
        investData.setInvestMode(investMode);
        investData.setClientType(clientType);
        // 事务执行前日志
        LOGGER.info("投资方法开始：MemberId:{},ProductItemId:{},Amount:{}", memberId, productItemId, amount.toString());
        InvestResult investResult = investTransaction(investData);
        LOGGER.info("投资方法结束：MemberId:{},ProductItemId:{},Amount:{}", memberId, productItemId, amount.toString());
        investResult.setMemberId(memberId);
        return investResult;
    }

    /**
     * 验证是否存在该会员,如果存在则返回
     * @param memberId
     * @return
     */
    private Member assertMember(String memberId){
        Member member = memberDao.getById(memberId);
        if (member == null) {
            LOGGER.debug("调试信息--MEMBER_NOT_EXIST,memberId:{}", memberId);
            throw new OctopusException(OctopusError.MEMBER_NOT_EXIST.getCode(), "用户不存在");
        }
        return member;
    }

    /**
     * 验证交易密码是否正确
     * @param investMode
     * @param tradePassword
     * @param member
     */
    private void assertTradePassword(String investMode, String tradePassword, Member member){
        if ((StringUtils.isBlank(investMode) || !InvestMode.AUTO.name().equals(investMode))
                && !Digest.shaDigest(tradePassword).equals(member.getTradePassword())) {
            throw new OctopusException(OctopusError.TRADE_PASSWORD_ERROR.getCode(), "交易密码错误");
        }
    }

    /**
     * 验证产品项是否存在,如果存在则返回
     * @param productItemId
     * @return
     */
    private ProductItem assertProductItem(String productItemId){
        ProductItem productItem = productItemDao.getById(productItemId);
        if (null == productItem || null == productItem.getProductId()
                || null == productItem.getItemSettingId()) {
            LOGGER.debug("调试信息--DATA_NOT_FOUND(productItem, productItem.getProductId, " +
                            "productItem.getItemSettingId), productItemId:{}",
                    productItemId);
            throw new OctopusException(OctopusError.DATA_NOT_FOUND.getCode(), "产品项不存在");
        }
        return productItem;
    }

    /**
     * 验证产品是否存在,如果存在则返回
     * @param productItem
     * @return
     */
    private Product assertProduct(ProductItem productItem){
        Product product = productDao.getProduct(productItem.getProductId());
        if (null == product) {
            LOGGER.debug("调试信息--DATA_NOT_FOUND(product), productId:{}", productItem.getProductId());
            throw new OctopusException(OctopusError.DATA_NOT_FOUND.getCode(), "产品不存在");
        }
        return product;
    }

    /**
     * 验证产品项配置是否存在,如果存在则返回
     * @param productItem
     * @return
     */
    private ProductItemSetting assertProductItemSetting(ProductItem productItem){
        ProductItemSetting productItemSetting = productItemSettingDao.getById(productItem.getItemSettingId());
        if (null == productItemSetting) {
            LOGGER.debug("调试信息--DATA_NOT_FOUND(productItemSetting), productItemSettingId:{}",
                    productItem.getItemSettingId());
            throw new OctopusException(OctopusError.DATA_NOT_FOUND.getCode(), "产品项设置不存在");
        }
        return productItemSetting;
    }

    /**
     * 验证产品项是否可以投资
     * 1.验证当前产品项是否处于可投资状态
     * 2.验证产品项的投资已完成金额是否小于总金额
     * 3.验证当前产品项是否处于可投资的时间
     * @param productItem
     */
    private void assertProductItemCanInvest(ProductItem productItem){
        if ((!ProductStatus.APPROVED.equals(productItem.getStatus())
                || productItem.getAmount().compareTo(productItem.getCompletedAmount()) <= 0
                || productItem.getReleaseTime().compareTo(new Date()) > 0)) {
            LOGGER.debug("调试信息--PRODUCT_NOT_INVEST, productItemStatus:{}, totalAmount:{}, " +
                            "completedAmount:{}, releaseTime:{}",
                    productItem.getStatus(), productItem.getAmount(), productItem.getCompletedAmount(),
                    DateUtils.fmtDate(productItem.getReleaseTime(), DateUtils.YYYY_MM_DD_HH_MM_SS_SSS));
            throw new OctopusException(OctopusError.PRODUCT_NOT_INVEST.getCode(), "当前产品不可投资");
        }
    }

    /**
     * 验证投资金额是否满足以下几个条件
     * 1.投资金额必须大于零
     * 2.投资金额必须小于当前产品项所剩投资金额
     * 3.投资金额必须大于当前产品项单笔最小投资金额
     * 4.投资后当前借款所剩的投资金额必须大于当前产品项最小投资金额
     * @param amount
     * @param productItem
     * @param productItemSetting
     */
    private void assertInvestAmount(BigDecimal amount, ProductItem productItem, ProductItemSetting productItemSetting){
        // 投资金额必须大于零
        if (amount.compareTo(new BigDecimal(0)) <= 0) {
            LOGGER.debug("调试信息--AMOUNT_MUST_MORE_THAN_ZERO, amount:{}", amount);
            throw new OctopusException(OctopusError.AMOUNT_MUST_MORE_THAN_ZERO.getCode(), "投资金额必须大于零");
        }
        // 验证投资金额是否大于当前产品项所剩投资金额
        BigDecimal remainAmount = productItem.getAmount().subtract(productItem.getCompletedAmount());
        if (amount.compareTo(remainAmount) == 1) {
            LOGGER.debug("调试信息--GREATER_THEN_REMAIN_INVEST_AMOUNT, amount:{}, remainAmount:{}",
                    amount, remainAmount);
            throw new OctopusException(OctopusError.GREATER_THEN_REMAIN_INVEST_AMOUNT.getCode(), "标的剩余金额不足");
        }
        // 验证投资金额是否小于当前产品项单笔最小投资金额
        if (amount.compareTo(productItemSetting.getMinInvestmentAmount()) == -1) {
            LOGGER.debug("调试信息--LESS_THAN_MIN_INVEST_AMOUNT, amount:{}, minInvestmentAmount:{}",
                    amount, productItemSetting.getMinInvestmentAmount());
            throw new OctopusException(OctopusError.LESS_THAN_MIN_INVEST_AMOUNT.getCode(), "小于最小投资金额");
        }
        // 验证投资后当前借款所剩的投资金额是否小于当前产品项最小投资金额
        BigDecimal afterRemainAmountBigDecimal = remainAmount.subtract(amount);
        if (afterRemainAmountBigDecimal.compareTo(productItemSetting.getMinInvestmentAmount()) == -1
                && afterRemainAmountBigDecimal.compareTo(new BigDecimal(0)) == 1) {
            LOGGER.debug("调试信息--REMAIN_INVEST_AMOUNT_LESS_THAN_MIN_INVEST_AMOUNT, " +
                            "afterRemainAmountBigDecimal:{}, minInvestmentAmount:{}",
                    afterRemainAmountBigDecimal, productItemSetting.getMinInvestmentAmount());
            throw new OctopusException(OctopusError.REMAIN_INVEST_AMOUNT_LESS_THAN_MIN_INVEST_AMOUNT.getCode(),
                    "剩余金额不能小于最小投资金额");
        }
    }

    /**
     * 验证该产品项是否可以让机构投资
     * @param productItem
     * @param member
     */
    private void assertCompanyCanInvest(ProductItem productItem, Member member){
        ProductCategorySetting productCategorySetting = productCategorySettingDao
                .getByCategoryIdAndProperty(productItem.getProductCategoryId(),
                        ProductDictProperty.CompanyInvestEnabled.name());
        if(productCategorySetting==null){
            LOGGER.debug("调试信息--DATA_NOT_FOUND(productCategorySetting), productCategoryId:{},productDictProperty:{}",
                    productItem.getProductCategoryId(),ProductDictProperty.CompanyInvestEnabled.name());
            throw new OctopusException(OctopusError.DATA_NOT_FOUND.getCode(), "productCategorySetting不存在");
        }
        if (MemberType.COMPANY.name().equals(member.getMemberType())
                 && !Boolean.valueOf(productCategorySetting.getValue())) {
            throw new OctopusException(OctopusError.COMPANY_NOT_INVEST.getCode(),
                    "投资失败:企业用户目前只能购买企业用户指定购买的产品!");
        }
    }

    /**
     * 验证当前产品项投资信息productItemInvestInfo是否存在
     * @param productItemId
     * @return
     */
    private ProductItemInvestInfo assertProductItemInvestInfo(String productItemId){
        ProductItemInvestInfo productItemInvestInfo = productItemInvestInfoDao.getByItemId(productItemId);
        if (null == productItemInvestInfo) {
            LOGGER.debug("调试信息--DATA_NOT_FOUND(productItemInvestInfo), productItemId:{}", productItemId);
            throw new OctopusException(OctopusError.DATA_NOT_FOUND.getCode(), "产品投资信息不存在");
        }
        return productItemInvestInfo;
    }

    /**
     * 验证当前产品是否已经流标
     */
    private void assertProductIsExpired(ProductType productType,ProductItemInvestInfo productItemInvestInfo){
        // 政企通和政金宝和票据的流标开关设置为NO时 流标时间为空 此处需要判断下
        if (productType == ProductType.ANXIN || productType == ProductType.AXB || productType == ProductType.PIAOJU) {
            if (productItemInvestInfo.getAllowExpired() != null && productItemInvestInfo.getAllowExpired().equals("YES")) {
                if (productItemInvestInfo.getExpiredTime().compareTo(new Date()) <= 0) {
                    throw new OctopusException(OctopusError.PRODUCT_NOT_INVEST.getCode(), "当前产品已经流标");
                }
            }
        } else {
            if (productItemInvestInfo.getExpiredTime().compareTo(new Date()) <= 0) {
                throw new OctopusException(OctopusError.PRODUCT_NOT_INVEST.getCode(), "当前产品已经流标");
            }
        }
    }

    /**
     * 验证当前产品是否已满标
     * @param productItem
     */
    private void assertProductIsFull(ProductItem productItem){
        if (ProductStatus.FULL.equals(productItem.getStatus())) {
            LOGGER.debug("调试信息--PRODUCT_NOT_INVEST, status:{}",
                    productItem.getStatus());
            throw new OctopusException(OctopusError.PRODUCT_NOT_INVEST.getCode(), "当前产品已经满标");
        }
    }

    /**
     * 投资金额的最小单位不能小于0.01元
     */
    private void assertMinInvestAmount(BigDecimal amount,ProductType productType){
        if(amount.subtract(amount.setScale(2, BigDecimal.ROUND_DOWN)).compareTo(new BigDecimal(0)) > 0
                && amount.subtract(amount.setScale(2, BigDecimal.ROUND_DOWN)).compareTo(new BigDecimal(0.01)) < 0){
            LOGGER.debug("调试信息--LESS_THAN_PENNY, productType:{}, amount:{}", productType, amount);
            throw new OctopusException(OctopusError.LESS_THAN_PENNY.getCode(), "投资金额的最小单位不能小于0.01元");
        }
    }

    /**
     * 验证投资金额是否满足递增条件
     */
    private void assertIncreasedAmount(BigDecimal amount,ProductItemSetting productItemSetting,ProductType productType){
        BigDecimal increaseCount = amount.subtract(productItemSetting.getMinInvestmentAmount())
                .divide(productItemSetting.getIncreaseBy(), 4, BigDecimal.ROUND_HALF_EVEN);
        BigDecimal increaseCountInt = increaseCount.setScale(0, BigDecimal.ROUND_DOWN);
        if (increaseCount.subtract(increaseCountInt).compareTo(new BigDecimal(0)) != 0) {
            LOGGER.debug("调试信息--INCREASE_BY_ERROR, productType:{}, amount:{}, increaseBy:{}",
                    productType, amount, productItemSetting.getIncreaseBy());
            throw new OctopusException(OctopusError.INCREASE_BY_ERROR.getCode(), "投资金额不符合递增条件");
        }
    }

    /**
     * 验证投资用户的主账户,如果存在则返回
     */
    private MemberAccount assertPrimeAccount(String memberId){
        MemberAccount primeAccount = memberAccountDao.getByMemberIdAndType(memberId, AccountType.PRIMARY.name());
        if (null == primeAccount) {
            LOGGER.debug("调试信息--DATA_NOT_FOUND(primeAccount), memberId:{}, accountType:{}",
                    memberId, AccountType.PRIMARY);
            throw new OctopusException(OctopusError.DATA_NOT_FOUND.getCode(), "主账户不存在");
        }
        return primeAccount;
    }

    /**
     * 验证主账户余额是否大于投资金额
     * @param amount
     * @param primeAccount
     */
    private void assertPrimeAccountBalance(BigDecimal amount,MemberAccount primeAccount){
        if (primeAccount.getAmountBalance().compareTo(amount) == -1) {
            LOGGER.debug("调试信息--ACCOUNT_BALANCE_INSUFFICIENT, amount:{}, amountBalance:{}",
                    amount, primeAccount.getAmountBalance());
            throw new OctopusException(OctopusError.ACCOUNT_BALANCE_INSUFFICIENT.getCode(), "账户余额小于投资金额");
        }
    }

    /**
     * 验证随鑫宝账户是否存在
     * @return
     */
    private MemberAccount assertXpressAccount(String memberId){
        MemberAccount xpressAccount = memberAccountDao.getByMemberIdAndType(memberId, AccountType.XPRESS.name());
        if (null == xpressAccount) {
            LOGGER.debug("调试信息--DATA_NOT_FOUND(xpressAccount), memberId:{}, accountType:{}",
                    memberId, AccountType.XPRESS);
            throw new OctopusException(OctopusError.DATA_NOT_FOUND.getCode(), "随鑫宝账户不存在");
        }
        return xpressAccount;
    }

    /**
     * 验证随鑫宝
     */
    private void assertXpress(String memberId,MemberAccount xpressAccount,BigDecimal amount,ProductItemSetting productItemSetting){
        // 查询投资记录（通过会员编号和产品类型）
        Date currentDate = DateUtils.getCurrentDate();
        List<MemberInvestment> memberInvestments = memberInvestmentDao
                .findByMemberIdAndProductType(memberId, ProductType.XPRESS.name(),
                        currentDate, DateUtils.addDays(currentDate, 1));
        // 验证投资金额是否大于当前产品项累计最大限投金额(增加随鑫宝白名单校验)
        XpressWhitelist xpressWhitelist = xpressWhitelistDao.getXpressWhitelistByMemberId(memberId);
        if (xpressWhitelist != null) {
            if (amount.compareTo(xpressWhitelist.getPurchaseLimit()
                    .subtract(xpressAccount.getTotalAmount()
                            .setScale(2, BigDecimal.ROUND_DOWN))) > 0)
                throw new OctopusException(OctopusError.GREATER_THAN_SUM_MAX_AMOUNT.getCode(),
                        "大于当前产品项累计最大限投金额");
        } else {
            // 投资次数限制：每天最多只能投资一次
            if (memberInvestments.size() > 0) {
                LOGGER.debug("调试信息--INVEST_ONLY_ONCE_PER_DAY, memberInvestments.size:{}", memberInvestments.size());
                throw new OctopusException(OctopusError.INVEST_ONLY_ONCE_PER_DAY.getCode(), "每天最多只能投资一次");
            }
            if (amount.compareTo(productItemSetting.getMaxInvestmentAmount()
                    .subtract(xpressAccount.getTotalAmount().setScale(2, BigDecimal.ROUND_DOWN))) > 0)
                throw new OctopusException(OctopusError.GREATER_THAN_SUM_MAX_AMOUNT.getCode(),
                        "大于当前产品项累计最大限投金额");
        }
    }

    /**
     * 验证新手标,只有理财产品鑫稳赢类的才有新手标
     */
    private void assertNovice(String memberId,ProductItem productItem,ProductItemSetting productItemSetting,BigDecimal amount){
        if("Y".equals(productItem.getIsNovice())){
            if(memberInvestmentDao.countByMemberIdExceptExperience(memberId) > 0) {
                LOGGER.debug("调试信息--ONLY_FIRST_INVESTMENT_CAN_BE_NOVICE");
                throw new OctopusException(OctopusError.ONLY_FIRST_INVESTMENT_CAN_BE_NOVICE.getCode(), "新手标必须是第一次投资");
            } else {
                // 投资上限
                BigDecimal investmentQuota = productItemSetting.getInvestmentQuota();
                if (investmentQuota == null) {
                    LOGGER.debug("调试信息--NOVICE_INVEST_MAX_AMOUNT_NOT_SETTING");
                    throw new OctopusException(OctopusError.GEN_INTERNAL_ERROR.getCode(), "投资失败，未设定投资限额");
                }
                // 金额限制
                if (amount.compareTo(investmentQuota) == 1) {
                    LOGGER.debug("调试信息--NOVICE_INVEST_AMOUNT_MORE_THAN_MAX_AMOUNT");
                    throw new OctopusException(OctopusError.NOVICE_INVEST_AMOUNT_MORE_THAN_MAX_AMOUNT.getCode(), "该新手标投资上限不能超过" + investmentQuota + "元");
                }
            }
        }
    }

    /**
     * 准备投资数据
     *
     * @param memberId      用户ID
     * @param productItemId 产品项ID
     * @param amount        投资金额
     * @param investMode    投资类型
     * @param tradePassword 交易密码MD5  @return 投资数据
     */
    private InvestData prepareAndValidInvestData(String memberId, String productItemId, BigDecimal amount,
                                                 String tradePassword, String investMode, Long memberCouponId) {
        //准备常用数据=====================================
        InvestData investData = new InvestData(memberId, productItemId, amount);
        // 验证当前用户是否存在
        Member member = assertMember(memberId);
        // 验证交易密码是否正确
        assertTradePassword(investMode,tradePassword,member);
        // 验证当前产品项是否存在
        ProductItem productItem = assertProductItem(productItemId);
        // 验证当前产品是否存在
        Product product = assertProduct(productItem);
        // 验证当前产品项配置是否存在
        ProductItemSetting productItemSetting = assertProductItemSetting(productItem);

        /**  ======逻辑性校验====== **/
        // 验证产品项是否可以投资
        assertProductItemCanInvest(productItem);
        // 验证投资金额是否满足一些条件
        assertInvestAmount(amount,productItem,productItemSetting);
        // 判断公司会员是否可以投资当前产品
        assertCompanyCanInvest(productItem,member);

        // 构造结果数据
        investData.setProductType(productItem.getType());
        investData.setProductItemName(productItem.getName());
        investData.setIsNovice(productItem.getIsNovice());
        investData.setProductCategoryId(productItem.getProductCategoryId());
        investData.setMemberCouponId(memberCouponId);
        investData.setProductItemSetting(productItemSetting);


        /** ======非通用逻辑性校验====== **/
        ProductType productType = ProductType.valueOf(product.getType());
        // 产品类型为体验金
        if (productType == ProductType.EXPERIENCE) {
            LOGGER.debug("调试信息--EXPERIENCE, 校验提前完成");
            return investData;
        }
        // 对于投资类产品
        if (productType == ProductType.INVESTMENT || productType == ProductType.YGB
                || productType == ProductType.ANXIN || productType == ProductType.AXB || productType == ProductType.PIAOJU) {
            // 验证当前产品项投资信息productItemInvestInfo是否存在
            ProductItemInvestInfo productItemInvestInfo = assertProductItemInvestInfo(productItemId);
            // 验证当前产品是否已经流标
            assertProductIsExpired(productType, productItemInvestInfo);
            // 验证当前产品是否已经满标
            assertProductIsFull(productItem);
        }

        // 对于投资类产品+随鑫宝
        if ((productType == ProductType.INVESTMENT || productType == ProductType.XPRESS
                || productType == ProductType.YGB || productType == ProductType.PIAOJU
                || productType == ProductType.ANXIN || productType == ProductType.AXB)) {
            // 投资金额的最小单位不能小于0.01元
            assertMinInvestAmount(amount,productType);
        }

        // 对于理财类产品
        if (productType == ProductType.FINANCIAL || productType == ProductType.PROFIT) {
            // 验证投资金额是否满足递增条件
            assertIncreasedAmount(amount, productItemSetting, productType);
        }

        // 验证主账户
        MemberAccount primeAccount = assertPrimeAccount(memberId);
        investData.setPrimeAccount(primeAccount);
        // 验证用户账户余额是否小于投资金额
        assertPrimeAccountBalance(amount,primeAccount);

        /** ======随鑫宝独有校验逻辑====== **/
        if (productType == ProductType.XPRESS) {
            // 查询随鑫宝账户
            MemberAccount xpressAccount = assertXpressAccount(memberId);
            investData.setXpressAccount(xpressAccount);
            // 验证随鑫宝相关逻辑
            assertXpress(memberId,xpressAccount,amount,productItemSetting);
        }

        /** ======新手标独有逻辑====== **/
        //新手标独有校验 是否第一次投资,投资限额
        if (productType == ProductType.FINANCIAL) {
            assertNovice(memberId,productItem,productItemSetting,amount);
        }
        /** ======使用加息券前相关的逻辑校验====== **/
        couponValidate(investData);
        return investData;
    }

    /**
     * 账户总额+投资金额<=最大投资金额 为合理情况，
     */
    private boolean xpressMaxInvestmentAmountCheck(InvestData investData) {
        XpressWhitelist xpressWhitelist = xpressWhitelistDao.getXpressWhitelistByMemberId(investData.getMemberId());
        BigDecimal maxInvestmentAmount;
        if (xpressWhitelist != null) {
            maxInvestmentAmount = xpressWhitelist.getPurchaseLimit();
        } else {
            maxInvestmentAmount = investData.getProductItemSetting().getMaxInvestmentAmount();
        }
        BigDecimal totalAmount = investData.getXpressAccount().getTotalAmount().setScale(2, BigDecimal.ROUND_DOWN);
        BigDecimal amount = investData.getAmount();
        return totalAmount.add(amount).compareTo(maxInvestmentAmount) < 1;
    }

    /**
     * 更新产品项表，获取当前产品的执行权限
     *
     * @return 是否满标
     */
    private boolean updateProductItem(InvestData investData) {
        String productItemId = investData.getProductItemId();
        BigDecimal amount = investData.getAmount();
        BigDecimal minInvestmentAmount = investData.getProductItemSetting().getMinInvestmentAmount();
        ProductType productType = investData.getProductType();
        //更新产品项表
        if (productItemDao.updateCompletedAmount(productItemId, amount, minInvestmentAmount) == 0
                || (productType == ProductType.XPRESS && !xpressMaxInvestmentAmountCheck(investData))) {
            LOGGER.debug("调试信息--UPDATE_FAIL(productItemDao.updateCompletedAmount), " +
                            "productItemId:{}, amount:{}, minInvestmentAmount:{}",
                    productItemId, amount, minInvestmentAmount);
            throw new OctopusException(OctopusError.UPDATE_FAIL.getCode(), "更新产品项完成金额失败");
        }
        //同步investData中产品项数据，使其和数据库数据一致
        ProductItem productItem = productItemDao.getById(productItemId);
        LOGGER.info("成功更新ProductItem, ID{}", productItemId);
        // 更新满标状态 
        if (productItem.getAmount().compareTo(productItem.getCompletedAmount()) == 0) {
            Map<String, Object> params = new HashMap<>();
            params.put("id", productItemId);
            params.put("finishTime", new Date());
            switch (productType) {
                case INVESTMENT:
                case YGB:
                case ANXIN:
                case AXB:
                case PIAOJU:
                    params.put("status", ProductStatus.FULL);
                    break;
                case FINANCIAL:
                case XPRESS:
                case EXPERIENCE:
                case PROFIT:
                    params.put("status", ProductStatus.REPAYMENT);
                    break;
                default:
                    break;
            }
            //todo 待跟踪枚举传值情况
            if (productItemDao.update(params) == 0) {
                LOGGER.debug("调试信息--UPDATE_FAIL, params:{}", CustomConverter.objectToString(params));
                throw new OctopusException(OctopusError.UPDATE_FAIL.getCode(), "更新产品项失败");
            }
            LOGGER.info("成功更新满标状态ProductItem, ID{}", productItem);
            return true;
        }
        return false;
    }

    /**
     * 生成账户资金快照
     */
    private void accountSnapshot(InvestData investData) {
        ProductType productType = investData.getProductType();
        if (productType != ProductType.EXPERIENCE) {
            // 新增（主）账户资金快照
            LOGGER.info("新增账户快照成功MemberAccountSnapshot, ID{}",
                    createAccountSnapshot(investData.getPrimeAccount()));
        }
        if (productType == ProductType.XPRESS) {
            // 新增随鑫宝账户资金快照
            LOGGER.info("新增随鑫宝账户快照成功MemberAccountSnapshot, ID{}",
                    createAccountSnapshot(investData.getXpressAccount()));
        }
    }

    /**
     * 更新账户
     */
    private void updateAccount(InvestData investData) {
        ProductType productType = investData.getProductType();
        BigDecimal amount = investData.getAmount();
        if (productType != ProductType.EXPERIENCE) {
            MemberAccount primeAccount = investData.getPrimeAccount();
            // 更新主账户表
            if (memberAccountDao.updatePrimeAccountOnInvesting(primeAccount.getId(), amount,
                    productType == ProductType.INVESTMENT
                            || productType == ProductType.YGB
                            || productType == ProductType.ANXIN
                            || productType == ProductType.AXB
                            || productType == ProductType.PIAOJU) == 0) {
                LOGGER.debug("调试信息--UPDATE_FAIL(updatePrimeAccountOnInvesting), " +
                                "accountId:{}, amount:{}, productType:{}",
                        primeAccount.getId(), amount, productType);
                throw new OctopusException(OctopusError.UPDATE_FAIL.getCode(), "更新主账户失败");
            }
            investData.setPrimeAccount(memberAccountDao.getByMemberIdAndType(investData.getMemberId(), AccountType.PRIMARY.name()));
            LOGGER.info("更新主账户成功MemberAccount, ID{}", primeAccount.getId());
        }
        if (productType == ProductType.XPRESS) {
            MemberAccount xpressAccount = investData.getXpressAccount();
            // 更新随鑫宝账户表
            if (memberAccountDao.updateXpressAccountOnInvesting(xpressAccount.getId(), amount) == 0) {
                LOGGER.debug("调试信息--UPDATE_FAIL(updateXpressAccountOnInvesting), accountId:{}, amount:{}",
                        xpressAccount.getId(), amount);
                throw new OctopusException(OctopusError.UPDATE_FAIL.getCode(), "更新随鑫宝账户失败");
            }
            investData.setXpressAccount(memberAccountDao.getByMemberIdAndType(xpressAccount.getMemberId(), AccountType.XPRESS.name()));
            LOGGER.info("更新主账户成功xpressAccount, ID{}", xpressAccount.getId());
        }
    }

    /**
     * 添加投资记录
     *
     * @param investData 投资数据
     * @return 投资记录Id
     */
    private String insertInvestment(InvestData investData) {
        ProductType productType = investData.getProductType();
        // 产生投资记录
        MemberInvestment memberInvestment = new MemberInvestment();
        memberInvestment.setMemberId(investData.getMemberId());
        memberInvestment.setProductItemId(investData.getProductItemId());
        memberInvestment.setAmount(investData.getAmount());
        memberInvestment.setInvestmentChannel("");
        memberInvestment.setClientType(investData.getClientType());
        if (!StringUtils.isEmpty(investData.getInvestMode())) {
            memberInvestment.setInvestMode(investData.getInvestMode());
        } else {
            memberInvestment.setInvestMode(InvestMode.MANUAL.name());
        }
        switch (productType) {
            case INVESTMENT:
            case YGB:
            case XPRESS:
            case ANXIN:
            case AXB:
            case PIAOJU:
                memberInvestment.setStatus(InvestmentStatus.INVESTING.name());
                break;
            case FINANCIAL:
            case PROFIT:
            case EXPERIENCE:
                memberInvestment.setStatus(InvestmentStatus.RECEIPTING.name());
                break;
            default:
                break;
        }
        memberInvestmentDao.insert(memberInvestment);
        LOGGER.info("新增投资记录成功, memberInvestment ID{}", memberInvestment.getId());
        return memberInvestment.getId();
    }

    /**
     * 增加资金记录
     *
     * @param investmentId 投资记录ID
     */
    private void insertFundRecord(InvestData investData, String investmentId) {
        ProductType productType = investData.getProductType();
        String productItemId = investData.getProductItemId();
        String memberId = investData.getMemberId();
        BigDecimal amount = investData.getAmount();
        if (productType != ProductType.EXPERIENCE) {
            MemberAccount primeAccount = investData.getPrimeAccount();
            // 产生资金记录(主账户)
            MemberFundRecord memberFundRecord = new MemberFundRecord();
            memberFundRecord.setMemberId(memberId);
            memberFundRecord.setMemberAccountId(primeAccount.getId());
            memberFundRecord.setAccountBalance(primeAccount.getAmountBalance());
            memberFundRecord.setFundAmount(amount);
            memberFundRecord.setInvestmentId(investmentId);
            memberFundRecord.setProductItemId(productItemId);
            memberFundRecord.setClientType(investData.getClientType());
            if (!StringUtils.isEmpty(investData.getInvestMode())) {
                memberFundRecord.setInvestMode(investData.getInvestMode());
            } else {
                memberFundRecord.setInvestMode(InvestMode.MANUAL.name());
            }
            switch (productType) {
                case INVESTMENT:
                case YGB:
                case XPRESS:
                case ANXIN:
                case AXB:
                case PIAOJU:
                    memberFundRecord.setType(MemberFundRecord.Type.FROZENINVESTMENT);
                    break;
                case FINANCIAL:
                case PROFIT:
                    memberFundRecord.setType(MemberFundRecord.Type.SUCCESSINVESTMENT);
                    break;
                default:
                    break;
            }
            String fundRemark = "投资[<a href=/product/productDetails?productItemId=%s target=_blank >%s</a>]";
            if (!StringUtils.isEmpty(investData.getInvestMode()) && investData.getInvestMode().equals(InvestMode.AUTO.name())) {
                fundRemark = "自动投资[<a href=/product/productDetails?productItemId=%s target=_blank >%s</a>]";
            }
            switch (productType) {
                case INVESTMENT:
                case YGB:
                case PROFIT:
                case ANXIN:
                case AXB:
                case PIAOJU:
                    memberFundRecord.setRemark(String.format(fundRemark, productItemId,
                            investData.getProductItemName()));
                    break;
                case FINANCIAL:
                    memberFundRecord.setRemark(String.format(fundRemark, productItemId,
                            "Y".equals(investData.getIsNovice()) ?
                                    investData.getProductItemName() + "（新手）" :
                                    investData.getProductItemName()));
                    break;
                case XPRESS:
                    memberFundRecord.setRemark("随鑫宝-投资");
                default:
                    break;
            }
            LOGGER.info("投资：MemberId:{},ProductItemId:{},Amount:{},InvestmentId:{}",
                    memberId, productItemId, amount.toString(), investmentId);
            fundRecordDao.insert(memberFundRecord);
            LOGGER.info("新增资金记录成功, memberFundRecord ID{}", memberFundRecord.getId());
        }
        if (productType == ProductType.XPRESS) {
            // 产生随鑫宝账户对应的资金记录
            MemberAccount xpressAccount = investData.getXpressAccount();
            MemberFundRecord xpressFundRecord = new MemberFundRecord();
            xpressFundRecord.setMemberId(memberId);
            xpressFundRecord.setMemberAccountId(xpressAccount.getId());
            xpressFundRecord.setAccountBalance(xpressAccount.getTotalAmount());
            xpressFundRecord.setFundAmount(amount);
            xpressFundRecord.setType(MemberFundRecord.Type.FROZENINVESTMENT);
            xpressFundRecord.setRemark("随鑫宝-投资");
            xpressFundRecord.setInvestmentId(investmentId);
            xpressFundRecord.setProductItemId(productItemId);
            xpressFundRecord.setClientType(investData.getClientType());
            fundRecordDao.insert(xpressFundRecord);
            LOGGER.info("新增随鑫宝资金记录成功, xpressFundRecord ID{}", xpressFundRecord.getId());
            // 添加随鑫宝账户对应的投资冻结资金记录
            FundDelay fundDelay = new FundDelay();
            fundDelay.setFundRecordId(xpressFundRecord.getId());
            fundDelay.setStatus(FundDelayStatus.FREEZE.name());
            // 计算起息日期(T+1) todo new Date() 是否存在问题
            fundDelay.setUnfreezeTime(DateUtils.addDays(new Date(), 1));
            fundDelayDao.insert(fundDelay);
            LOGGER.info("新增冻结资金记录成功, fundDelay ID{}", fundDelay.getId());
        }
    }

    /**
     * 更新平台相关账户
     *
     * @param investData 投资数据
     */
    private void updateAllocateAccount(InvestData investData) {
        ProductType productType = investData.getProductType();
        BigDecimal amount = investData.getAmount();
        if (productType == ProductType.FINANCIAL || productType == ProductType.PROFIT) {
            // 待拨付账户余额增加
            if (platformAccountDao.updateInvest(amount) == 0) {
                throw new OctopusException(OctopusError.UPDATE_FAIL.getCode(), "更新平台账户投资操作失败");
            }
        } else if (productType == ProductType.XPRESS) {
            // 不同的用户 ，随鑫宝投资额进入不同的账户：普通用户：平台的随鑫宝账户余额增加(60%) 准备金余额增加(40%)；白名单用户(平台的准备经账户余额增加100%)。
            XpressWhitelist xpressWhitelist = xpressWhitelistDao.getXpressWhitelistByMemberId(investData.getMemberId());
            if (xpressWhitelist != null) {
                if (platformAccountDao.updateInvestXpress(new BigDecimal(0),amount.setScale(6, BigDecimal.ROUND_HALF_EVEN)) == 0) {
                    throw new OctopusException(OctopusError.UPDATE_FAIL.getCode(),"更新平台账户随鑫宝投资操作失败");
                }
            } else {
                if (platformAccountDao.updateInvestXpress(
                        amount.multiply(new BigDecimal(0.6)).setScale(6,BigDecimal.ROUND_HALF_EVEN),
                        amount.multiply(new BigDecimal(0.4)).setScale(6,BigDecimal.ROUND_HALF_EVEN)) == 0) {
                    throw new OctopusException(OctopusError.UPDATE_FAIL.getCode(),"更新平台账户随鑫宝投资操作失败");
                }
            }
        }
    }

    /**
     * 数据库事物相关操作
     *
     * @param investData 投资数据
     * @return 投资结果数据
     */
    @Transactional
    private InvestResult investTransaction(InvestData investData) {
        InvestResult result = new InvestResult();
        // 更新产品项表，获取当前产品的执行权限，记录满标状态
        result.setIsFull(updateProductItem(investData));
        // 生成快照
        accountSnapshot(investData);
        // 更新账户
        updateAccount(investData);
        // 生成投资记录
        result.setInvestmentId(insertInvestment(investData));
        result.setInvestTime(new Date());
        // 生成资金记录
        insertFundRecord(investData, result.getInvestmentId());
        // 更新待拨付资金账户
        updateAllocateAccount(investData);
        // 记录平台操作日志，便于第二次平台对账上限
        insertPlatformOperationLog(investData, result.getInvestmentId());
        // 使用加息券
        couponUse(result.getInvestmentId(), investData); 
        
        return result;
    }

    /**
     * 从收款计划中计算剩余本息,剩余天数,剩余期限,当前期数等值
     */
    private Map<String,Object> computeFromReceiptPlan(List<MemberReceiptPlan> memberReceiptPlans, String interestCalMode) {

        Map<String, Object> result = new HashMap<>();
        // 剩余本息
        BigDecimal remainAmount = new BigDecimal(0);
        // 剩余天数
        int remainDays = 0;
        // 剩余期限
        int remainTerms = 0;
        // 当前期数
        int currentPeriod = 0;

        Date currentTime = new Date();
        MemberReceiptPlan memberReceiptPlan = null;
        if (interestCalMode.equals(RepaymentType.Month.name())) {
            if (memberReceiptPlans.size() > 1) {
                // 获取当前期的收款计划
                for (int i = 0; i < memberReceiptPlans.size(); i++) {
                    if (i == 0) {
                        boolean flag = isBetweenTimeSpan(memberReceiptPlans.get(i).getInterestStartTime(),
                                memberReceiptPlans.get(i).getInterestEndTime(),
                                currentTime);
                        if (flag) {
                            memberReceiptPlan = memberReceiptPlans.get(i);
                        }
                    }
                    if (i > 0) {
                        boolean flag = isBetweenTimeSpan(memberReceiptPlans.get(i - 1).getInterestEndTime(),
                                memberReceiptPlans.get(i).getInterestEndTime(),
                                currentTime);
                        if (flag) {
                            memberReceiptPlan = memberReceiptPlans.get(i);
                        }
                    }
                }
                if (memberReceiptPlan != null) {
                    currentPeriod = memberReceiptPlan.getPeriod();
                    result.put("currentPeriod",currentPeriod);
                    // 计算剩余本息
                    for (int i = currentPeriod - 1; i < memberReceiptPlans.size(); i++) {
                        remainAmount = remainAmount.add(memberReceiptPlans.get(i).getSettlementAmount());
                        result.put("remainAmount",remainAmount);
                    }

                    // 计算剩余天数
                    int totalPeriods = memberReceiptPlans.size();
                    remainDays += getDaysBetween(currentTime, memberReceiptPlan.getInterestEndTime());
                    remainDays += (totalPeriods - currentPeriod) * 30;
                    result.put("remainDays",remainDays);

                    // 计算剩余期限
                    remainTerms = totalPeriods - currentPeriod + 1;
                    result.put("remainTerms", remainTerms);
                }
            }
        } else if (interestCalMode.equals(RepaymentType.Once.name())) {
            if (memberReceiptPlans.size() > 0) {
                memberReceiptPlan = memberReceiptPlans.get(0);
            }
            if (memberReceiptPlan != null) {
                // 计算剩余本息
                remainAmount = remainAmount.add(memberReceiptPlan.getSettlementAmount());
                result.put("remainAmount",remainAmount);

                // 计算剩余天数
                remainDays += getDaysBetween(currentTime, memberReceiptPlan.getInterestEndTime());
                result.put("remainDays",remainDays);

                // 计算剩余期限
                remainTerms = getDaysBetween(currentTime, memberReceiptPlan.getInterestEndTime());
                result.put("remainTerms",remainTerms);

                // 当前期数设置为1
                result.put("currentPeriod",1);
            }
        }
        return result;
    }

    /**
     * 计算剩余本息
     */
    @Deprecated
    private BigDecimal remainAmountCompute(List<MemberReceiptPlan> memberReceiptPlans, String interestCalMode) {
        BigDecimal remainAmount = new BigDecimal(0);
        Date currentTime = new Date();
        MemberReceiptPlan memberReceiptPlan = null;
        if (interestCalMode.equals(RepaymentType.Month.name())) {
            if (memberReceiptPlans.size() > 1) {
                // 获取当前期的收款计划
                for (int i = 0; i < memberReceiptPlans.size(); i++) {
                    if (i == 0) {
                        boolean flag = isBetweenTimeSpan(memberReceiptPlans.get(i).getInterestStartTime(),
                                memberReceiptPlans.get(i).getInterestEndTime(),
                                currentTime);
                        if (flag) {
                            memberReceiptPlan = memberReceiptPlans.get(i);
                        }
                    }
                    if (i > 0) {
                        boolean flag = isBetweenTimeSpan(memberReceiptPlans.get(i - 1).getInterestEndTime(),
                                memberReceiptPlans.get(i).getInterestEndTime(),
                                currentTime);
                        if (flag) {
                            memberReceiptPlan = memberReceiptPlans.get(i);
                        }
                    }
                }
                if (memberReceiptPlan != null) {
                    int currentPeriod = memberReceiptPlan.getPeriod();
                    // 计算剩余本息
                    for (int i = currentPeriod - 1; i < memberReceiptPlans.size(); i++) {
                        remainAmount = remainAmount.add(memberReceiptPlans.get(i).getSettlementAmount());
                    }
                }
            }
        } else if (interestCalMode.equals(RepaymentType.Once.name())) {
            if (memberReceiptPlans.size() > 0) {
                memberReceiptPlan = memberReceiptPlans.get(0);
            }
            if (memberReceiptPlan != null) {
                // 计算剩余本息
                remainAmount = remainAmount.add(memberReceiptPlan.getSettlementAmount());
            }
        }
        return remainAmount;
    }

    /**
     * 判断所给的时间是否在指定的时间段内
     */
    private static boolean isBetweenTimeSpan(Date startTime, Date endTime, Date compareTime) {
        Calendar cal = Calendar.getInstance();
        // 开始时间精确到天
        cal.setTime(startTime);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        startTime = cal.getTime();
        // 结束时间精确到天
        cal.setTime(endTime);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        endTime = cal.getTime();
        // 比较时间精确到天
        cal.setTime(compareTime);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        compareTime = cal.getTime();
        // 进行比较
        return compareTime.compareTo(startTime) > 0 && compareTime.compareTo(endTime) < 0;
    }

    /**
     * 计算两个日期相差的天数
     */
    private static int getDaysBetween(Date date1, Date date2) {
        if (date1.compareTo(date2) > 0) {
            Date tmp = date1;
            date1 = date2;
            date2 = tmp;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long time1 = cal.getTimeInMillis();
        cal.setTime(date2);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);
        if ((time2 - time1) % (1000 * 3600 * 24) != 0) {
            between_days += 1;
        }
        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 计算剩余天数
     */
    @Deprecated
    private int remainDaysCompute(List<MemberReceiptPlan> memberReceiptPlans, String interestCalMode) {
        int remainDays = 0;
        Date currentTime = new Date();
        MemberReceiptPlan memberReceiptPlan = null;
        if (interestCalMode.equals(RepaymentType.Month.name())) {
            // 剩余天数 = 剩余整月期限*30+ 本期受让方实际占用时间
            // 本期受让方实际占用时间 =本期收款日-当前日
            if (memberReceiptPlans.size() > 1) {
                // 获取当前期的收款计划
                for (int i = 0; i < memberReceiptPlans.size(); i++) {
                    if (i == 0) {
                        boolean flag = isBetweenTimeSpan(memberReceiptPlans.get(i).getInterestStartTime(),
                                memberReceiptPlans.get(i).getInterestEndTime(),
                                currentTime);
                        if (flag) {
                            memberReceiptPlan = memberReceiptPlans.get(i);
                        }
                    }
                    if (i > 0) {
                        boolean flag = isBetweenTimeSpan(memberReceiptPlans.get(i - 1).getInterestEndTime(),
                                memberReceiptPlans.get(i).getInterestEndTime(),
                                currentTime);
                        if (flag) {
                            memberReceiptPlan = memberReceiptPlans.get(i);
                        }
                    }
                }
                if (memberReceiptPlan != null) {
                    int totalPeriods = memberReceiptPlans.size();
                    int currentPeriod = memberReceiptPlan.getPeriod();
                    remainDays += getDaysBetween(currentTime, memberReceiptPlan.getInterestEndTime());
                    remainDays += (totalPeriods - currentPeriod) * 30;
                }
            }
        } else if (interestCalMode.equals(RepaymentType.Once.name())) {
            // 剩余天数 = 到期日-当前日
            if (memberReceiptPlans.size() > 0) {
                memberReceiptPlan = memberReceiptPlans.get(0);
            }
            if (memberReceiptPlan != null) {
                remainDays += getDaysBetween(currentTime, memberReceiptPlan.getInterestEndTime());
            }
        }
        return remainDays;
    }

    /**
     * 计算剩余期限
     */
    @Deprecated
    public int remainTermsCompute(List<MemberReceiptPlan> memberReceiptPlans, String interestCalMode) {
        int remainTerms = 0;
        Date currentTime = new Date();
        MemberReceiptPlan memberReceiptPlan = null;
        if (interestCalMode.equals(RepaymentType.Month.name())) {
            if (memberReceiptPlans.size() > 1) {
                // 获取当前期的收款计划
                for (int i = 0; i < memberReceiptPlans.size(); i++) {
                    if (i == 0) {
                        boolean flag = isBetweenTimeSpan(memberReceiptPlans.get(i).getInterestStartTime(),
                                memberReceiptPlans.get(i).getInterestEndTime(),
                                currentTime);
                        if (flag) {
                            memberReceiptPlan = memberReceiptPlans.get(i);
                        }
                    }
                    if (i > 0) {
                        boolean flag = isBetweenTimeSpan(memberReceiptPlans.get(i - 1).getInterestEndTime(),
                                memberReceiptPlans.get(i).getInterestEndTime(),
                                currentTime);
                        if (flag) {
                            memberReceiptPlan = memberReceiptPlans.get(i);
                        }
                    }
                }
                if (memberReceiptPlan != null) {
                    int totalPeriods = memberReceiptPlans.size();
                    int currentPeriod = memberReceiptPlan.getPeriod();
                    remainTerms = totalPeriods - currentPeriod + 1;
                }
            }
        } else if (interestCalMode.equals(RepaymentType.Once.name())) {
            if (memberReceiptPlans.size() > 0) {
                memberReceiptPlan = memberReceiptPlans.get(0);
            }
            if (memberReceiptPlan != null) {
                remainTerms = getDaysBetween(currentTime, memberReceiptPlan.getInterestEndTime());
            }
        }
        return remainTerms;
    }

    /**
     * 计算当前期数
     */
    @Deprecated
    public int currentPeriodCompute(List<MemberReceiptPlan> memberReceiptPlans, String interestCalMode) {
        int currentPeriod = 0;
        Date currentTime = new Date();
        MemberReceiptPlan memberReceiptPlan = null;
        if (interestCalMode.equals(RepaymentType.Month.name())) {
            if (memberReceiptPlans.size() > 1) {
                // 获取当前期的收款计划
                for (int i = 0; i < memberReceiptPlans.size(); i++) {
                    if (i == 0) {
                        boolean flag = isBetweenTimeSpan(memberReceiptPlans.get(i).getInterestStartTime(),
                                memberReceiptPlans.get(i).getInterestEndTime(),
                                currentTime);
                        if (flag) {
                            memberReceiptPlan = memberReceiptPlans.get(i);
                        }
                    }
                    if (i > 0) {
                        boolean flag = isBetweenTimeSpan(memberReceiptPlans.get(i - 1).getInterestEndTime(),
                                memberReceiptPlans.get(i).getInterestEndTime(),
                                currentTime);
                        if (flag) {
                            memberReceiptPlan = memberReceiptPlans.get(i);
                        }
                    }
                }
                if (memberReceiptPlan != null) {
                    currentPeriod = memberReceiptPlan.getPeriod();
                }
            }
        } else if (interestCalMode.equals(RepaymentType.Once.name())) {
            if (memberReceiptPlans.size() > 0) {
                memberReceiptPlan = memberReceiptPlans.get(0);
            }
            if (memberReceiptPlan != null) {
                currentPeriod = 1;
            }
        }
        return currentPeriod;
    }

    /**
     * 债权转让收款计划逻辑
     */
    private void updateReceiptPlan(InvestData investData, InvestResult investResult) {
        // 查询原收款计划
        List<MemberReceiptPlan> originalReceiptPlans = receiptPlanDao
                .findByInvestId(investData.getOriginalInvestment().getId());
        if (null == originalReceiptPlans || 0 == originalReceiptPlans.size()) {
            LOGGER.debug("调试信息--DATA_NOT_FOUND(originalReceiptPlans), investId:{}",
                    investData.getOriginalInvestment().getId());
            throw new OctopusException(OctopusError.DATA_NOT_FOUND.getCode(), "未能查询到原收款计划数据");
        }
        // 查询出计息方式
        String interestCalMode = investData.getProductItemSetting().getInterstCalMode();
        // 从收款计划中计算剩余本息,剩余天数,剩余期限,当前期数
        Map<String,Object> map = computeFromReceiptPlan(originalReceiptPlans,interestCalMode);
        // 计算剩余本息
        BigDecimal remainAmount = (BigDecimal)map.get("remainAmount");
        // 计算剩余天数
        int remainDays = (Integer)map.get("remainDays");
        // 计算剩余期限
        int remainTerms = (Integer)map.get("remainTerms");
        // 计算当前期的收款计划期数
        int currentPeriod = (Integer)map.get("currentPeriod");
        // 更新原收款计划
        for (int i = currentPeriod - 1; i < originalReceiptPlans.size(); i++) {
            MemberReceiptPlan originalReceiptPlan = originalReceiptPlans.get(i);
            Map<String, Object> params = new HashMap<>();
            params.put("memberInvestmentId", originalReceiptPlan.getMemberInvestmentId());
            params.put("period", originalReceiptPlan.getPeriod());
            params.put("status", ReceiptPlanStatus.TRANSFERED.name());
            if (0 == receiptPlanDao.update(params)) {
                LOGGER.debug("调试信息--UPDATE_FAIL(更新原收款计划失败), params:{}",
                        CustomConverter.objectToString(params));
                throw new OctopusException(OctopusError.UPDATE_FAIL.getCode(), "更新原收款计划失败");
            }
        }
        // 产生新的收款计划
        if (interestCalMode.equals(RepaymentType.Month.name())) {
            for (int i = currentPeriod - 1; i < originalReceiptPlans.size(); i++) {
                MemberReceiptPlan originalReceiptPlan = originalReceiptPlans.get(i);
                originalReceiptPlan.setId(null);
                originalReceiptPlan.setPeriod(i - currentPeriod + 2);
                originalReceiptPlan.setMemberInvestmentId(investResult.getInvestmentId());
                receiptPlanDao.insert(originalReceiptPlan);
            }
        } else if (interestCalMode.equals(RepaymentType.Once.name())) {
            MemberReceiptPlan originalReceiptPlan = originalReceiptPlans.get(0);
            originalReceiptPlan.setId(null);
            originalReceiptPlan.setPeriod(currentPeriod);
            originalReceiptPlan.setMemberInvestmentId(investResult.getInvestmentId());
            receiptPlanDao.insert(originalReceiptPlan);
        }
        LOGGER.info("新增收款计划成功!");
        // 计算年化收益率的分子=（剩余本息-转让价格）*360
        BigDecimal molecule = remainAmount.subtract(investData.getAmount()).multiply(new BigDecimal(360));
        // 计算年化收益率的分母=转让价格*剩余天数
        BigDecimal denominator = investData.getAmount().multiply(new BigDecimal(remainDays));
        // 计算年化收益率=分子/分母=（剩余本息-转让价格）*360/(转让价格*剩余天数)
        BigDecimal yearYield = molecule.divide(denominator, 4, BigDecimal.ROUND_DOWN);
        // 更新新品项设置表
        Map<String, Object> params = new HashMap<>();
        params.put("yearYield", yearYield);
        params.put("id", investData.getProductItemSetting().getId());
        params.put("term", remainTerms);
        if (0 == productItemSettingDao.update(params)) {
            LOGGER.debug("调试信息--UPDATE_FAIL(更新新品项设置表失败), params:{}", params);
            throw new OctopusException(OctopusError.UPDATE_FAIL.getCode(), "更新新品项设置表失败");
        }
    }

    /**
     * 投资后还款计划生成
     *
     * @param investmentId 投资ID
     */
    @Transactional
    public void investmentReceiptPlan(String investmentId) {
        MemberInvestment memberInvestment = memberInvestmentDao.getById(investmentId);
        if (null == memberInvestment) {
            LOGGER.debug("调试信息--DATA_NOT_FOUND(memberInvestment), investmentId:{}", investmentId);
            throw new OctopusException(OctopusError.DATA_NOT_FOUND.getCode(), "用户投资信息不存在");
        }
        BigDecimal amount = memberInvestment.getAmount();
        String productItemId = memberInvestment.getProductItemId();
        ProductItem productItem = productItemDao.getById(productItemId);
        ProductType productType = productItem.getType();
        ProductItemSetting productItemSetting = productItemSettingDao.getById(productItem.getItemSettingId());
        if (productType != ProductType.XPRESS) {
            // 产生收款计划
            // T+1日起息
            RepaymentType repaymentType = RepaymentType.valueOf(productItemSetting.getInterstCalMode());
            Date interestDate = DateUtils.calcInterestDate(productItemSetting);
            CalculationInput input = new CalculationInput();
            input.setAmount(amount);
            input.setInterestDate(interestDate);
            input.setInterestRates(productItemSetting.getYearYield());
            input.setPeriods(productItemSetting.getTerm());
            input.setRepaymentType(repaymentType);
            input.setPeriodType(getPeriodType(productItemSetting, repaymentType));
            List<CalculationOutput> outputs = Calculator.calculate(input);
            for (CalculationOutput output : outputs) {
                MemberReceiptPlan memberReceiptPlan = new MemberReceiptPlan();
                memberReceiptPlan.setMemberInvestmentId(memberInvestment.getId());
                memberReceiptPlan.setInterestAmount(output.getInterest());
                memberReceiptPlan.setInterestEndTime(output.getRepaymentDate());
                memberReceiptPlan.setActualInterestAmount(new BigDecimal(0));
                memberReceiptPlan.setActualSettlementAmount(new BigDecimal(0));
                memberReceiptPlan.setPeriod(output.getPeroid());
                // 此处和理财产品不同（体验金要回收）
                if (productType == ProductType.EXPERIENCE) {
                    memberReceiptPlan.setSettlementAmount(output.getInterest());
                    memberReceiptPlan.setPrincipeAmount(new BigDecimal("0.00"));
                    memberReceiptPlan.setStatus(ReceiptPlanStatus.RECEIPTING.name());
                    switch (repaymentType) {
                        case Month:
                            memberReceiptPlan.setInterestStartTime(DateUtils.addMonths(output.getRepaymentDate(), -1));
                            break;
                        case Once:
                            memberReceiptPlan.setInterestStartTime(interestDate);
                            break;
                        default:
                            break;
                    }
                } else {
                    memberReceiptPlan.setPrincipeAmount(output.getPrinciple());
                    memberReceiptPlan.setSettlementAmount(output.getPrincilpeAndInterest());
                    if (productType == ProductType.INVESTMENT || productType == ProductType.YGB
                            || productType == ProductType.ANXIN || productType == ProductType.AXB
                            || productType == ProductType.PIAOJU ) {
                        memberReceiptPlan.setStatus(ReceiptPlanStatus.INVESTING.name());
                        memberReceiptPlan.setInterestStartTime(DateUtils.addMonths(output.getRepaymentDate(), -1));
                    } else if (productType == ProductType.FINANCIAL) {
                        memberReceiptPlan.setStatus(ReceiptPlanStatus.RECEIPTING.name());
                        switch (repaymentType) {
                            case Month:
                                memberReceiptPlan.setInterestStartTime(
                                        DateUtils.addMonths(output.getRepaymentDate(), -1));
                                break;
                            case Once:
                                memberReceiptPlan.setInterestStartTime(interestDate);
                                break;
                            default:
                                break;
                        }
                    } else if (productType == ProductType.PROFIT) {
                        memberReceiptPlan.setStatus(ReceiptPlanStatus.RECEIPTING.name());
                        memberReceiptPlan.setInterestStartTime(DateUtils.addMonths(output.getRepaymentDate(), -1));
                    }
                }
                memberReceiptPlan.setCouponProfit(false);
                receiptPlanDao.insert(memberReceiptPlan);
                LOGGER.info("新增还款计划成功, memberReceiptPlan ID{}", memberReceiptPlan.getId());
                
                // 生成加息券收款计划
                if (productType == ProductType.FINANCIAL || productType == ProductType.PROFIT) {
                    couponReceiptPlan(memberInvestment, productItemSetting, memberReceiptPlan);
                } 
            }
        }
    }

    /**
     * 满标后还款计划生成
     *
     * @param productItemId 产品项Id
     */
    @Transactional
    public void investmentFullReceiptPlans(String productItemId) {
        if (null == productItemId) {
            LOGGER.debug("调试信息--PARAMS_CAN_NOT_BE_NULL(productItemId)");
            throw new OctopusException(OctopusError.PARAMS_CAN_NOT_BE_NULL.getCode(), "productItemId参数不能为空");
        }
        // 查询产品项
        ProductItem productItem = productItemDao.getById(productItemId);
        if (null == productItem) {
            LOGGER.debug("调试信息--DATA_NOT_FOUND(productItem), productItemId:{}", productItemId);
            throw new OctopusException(OctopusError.DATA_NOT_FOUND.getCode(), "产品项不存在");
        }
        ProductType productType = productItem.getType();
        if (!(productType == ProductType.INVESTMENT || productType == ProductType.YGB
                || productType == ProductType.ANXIN || productType == ProductType.AXB
                || productType == ProductType.PIAOJU )) {
            LOGGER.debug("调试信息--PRODUCT_TYPE_MUST_BE_INVESTMENT, productType:{}", productType);
            throw new OctopusException(OctopusError.PRODUCT_TYPE_MUST_BE_INVESTMENT.getCode(), "只支持投资类产品");
        }
        // 判断是否满标
        if (!productItem.getStatus().equals(ProductStatus.FULL)) {
            LOGGER.debug("调试信息--PRODUCT_NOT_FULL, productItemId:{}, status:{}",
                    productItemId, productItem.getStatus());
            throw new OctopusException(OctopusError.PRODUCT_NOT_FULL.getCode(), "产品没有满标");
        }
        // 查询产品项设置表
        ProductItemSetting productItemSetting = productItemSettingDao.getById(productItem.getItemSettingId());
        // 查询该产品项对应的投资记录表
        List<MemberInvestment> investmentList = memberInvestmentDao.getByItemId(productItem.getId());
        for (MemberInvestment investment : investmentList) {
            try {
                // 更新加息券状态
                couponUpdateStatus(investment, MemberCouponStatus.FROZEN,
                        MemberCouponStatus.USED);  
                
                BigDecimal amount = investment.getAmount();
                // 产生收款计划
                // T+1日起息
                RepaymentType repaymentType = RepaymentType.valueOf(productItemSetting.getInterstCalMode());
                Date interestDate = DateUtils.calcInterestDate(productItemSetting);
                CalculationInput input = new CalculationInput();
                input.setAmount(amount);
                input.setInterestDate(interestDate);
                input.setInterestRates(productItemSetting.getYearYield());
                input.setPeriods(productItemSetting.getTerm());
                input.setRepaymentType(repaymentType);
                input.setPeriodType(getPeriodType(productItemSetting, repaymentType));
                List<CalculationOutput> outputs = Calculator.calculate(input);
                List<MemberReceiptPlan> receiptPlanList = new ArrayList<>();
                for (CalculationOutput output : outputs) {
                    MemberReceiptPlan memberReceiptPlan = new MemberReceiptPlan();
                    memberReceiptPlan.setMemberInvestmentId(investment.getId());
                    memberReceiptPlan.setInterestAmount(output.getInterest());
                    memberReceiptPlan.setInterestEndTime(output.getRepaymentDate());
                    memberReceiptPlan.setActualInterestAmount(new BigDecimal(0));
                    memberReceiptPlan.setActualSettlementAmount(new BigDecimal(0));
                    memberReceiptPlan.setPeriod(output.getPeroid());
                    memberReceiptPlan.setPrincipeAmount(output.getPrinciple());
                    memberReceiptPlan.setSettlementAmount(output.getPrincilpeAndInterest());
                    memberReceiptPlan.setStatus(ReceiptPlanStatus.RECEIPTING.name());
                    memberReceiptPlan.setInterestStartTime(DateUtils.addMonths(output.getRepaymentDate(), -1));
                    receiptPlanList.add(memberReceiptPlan);
                }
                LOGGER.debug("满标还款计划数据准备结束");
                if (investment.getStatus().equals(InvestmentStatus.INVESTING.name())) {
                    LOGGER.debug("开始生成还款计划");
                    // 查询会员的主账户表
                    MemberAccount primeAccount = memberAccountDao.getByMemberIdAndType(investment.getMemberId(),
                            AccountType.PRIMARY.name());
                    // 添加主账户表的账户信息快照
                    String accountSnapshotId = createAccountSnapshot(primeAccount);
                    LOGGER.debug("调试信息--满标-新增主账户快照成功, accountSnapshotId:{}", accountSnapshotId);
                    // 减少账户冻结余额
                    if (memberAccountDao.updateUnfrozenAccountOnFull(primeAccount.getId(), amount) == 0) {
                        LOGGER.debug("调试信息--UPDATE_FAIL(updateUnfrozenAccountOnFull), accountId:{}, amount:{}",
                                primeAccount.getId(), amount);
                        throw new OctopusException(OctopusError.UPDATE_FAIL.getCode(), "减少账户冻结余额失败");
                    }
                    LOGGER.info("更新主账户表成功, memberAccount ID{}", primeAccount.getId());
                    
                    // 更新收款计划的状态，并产生新数据
                    for (MemberReceiptPlan receiptPlan : receiptPlanList) {
                        int i = receiptPlan.getPeriod();
                        receiptPlan.setInterestStartTime(DateUtils.addMonths(DateUtils.addDays(new Date(), 1),
                                i - 1));
                        if ("Once".equals(productItemSetting.getInterstCalMode())) {
                            if("DAY".equals(productItemSetting.getTermType())){
                                receiptPlan.setInterestEndTime(DateUtils.addDays(DateUtils.addDays(new Date(), 1), productItemSetting.getTerm()));
                            }else if("MONTH".equals(productItemSetting.getTermType())){
                                receiptPlan.setInterestEndTime(DateUtils.addMonths(addDays(new Date(), 1), productItemSetting.getTerm()));
                            }
                        }else if ("Month".equals(productItemSetting.getInterstCalMode())) {
                            receiptPlan.setInterestEndTime(DateUtils.addMonths(addDays(new Date(), 1), i));
                        }
                        receiptPlan.setCouponProfit(false);
                        receiptPlanDao.insert(receiptPlan);
                        LOGGER.debug("新增收款计划状态成功, receiptPlan ID{}", receiptPlan.getId());
                        
                        // 生成加息券收款计划
                        couponReceiptPlan(investment, productItemSetting, receiptPlan);
                    }
                    LOGGER.debug("新增收款计划完成");
                    
                    // 更新投资记录的状态
                    investment.setStatus(InvestmentStatus.RECEIPTING.name());
                    Map<String, Object> params = new HashMap<>();
                    params.put("id", investment.getId());
                    params.put("status", InvestmentStatus.RECEIPTING.name());
                    memberInvestmentDao.update(params);
                    // 查询更新后的主账户表
                    primeAccount = memberAccountDao.getByMemberIdAndType(investment.getMemberId(),
                            AccountType.PRIMARY.name());
                    // 产生资金记录
                    MemberFundRecord memberFundRecord = new MemberFundRecord();
                    memberFundRecord.setMemberId(investment.getMemberId());
                    memberFundRecord.setMemberAccountId(primeAccount.getId());
                    memberFundRecord.setAccountBalance(primeAccount.getAmountBalance());
                    memberFundRecord.setFundAmount(investment.getAmount());
                    memberFundRecord.setType(MemberFundRecord.Type.SUCCESSINVESTMENT);
                    LOGGER.info("投资产品-满标：MemberId:{},MemberAccountId:{},Amount:{}",
                            investment.getMemberId(), primeAccount.getId(), investment.getAmount());
                    memberFundRecord.setRemark("投资产品-满标");
                    memberFundRecord.setInvestmentId(investment.getId());
                    memberFundRecord.setProductItemId(productItemId);
                    fundRecordDao.insert(memberFundRecord);
                    LOGGER.info("新增资金记录成功, memberFundRecord ID{}", memberFundRecord.getId());
                    // 待拨付账户余额增加
                    if (platformAccountDao.updateInvest(investment.getAmount()) == 0) {
                        LOGGER.debug("调试信息--增加待拨付账户余额失败, amount:{}, investId:{}",
                                amount, investment.getId());
                        throw new OctopusException(OctopusError.UPDATE_FAIL.getCode(), "增加待拨付账户余额失败");
                    }
                }
            } catch (OctopusException e) {
                LOGGER.error("生成还款计划失败");
                throw e;
            } catch (Exception e) {
                LOGGER.error("生成满标资金记录失败！", e);
                throw new OctopusException(OctopusError.UPDATE_RECEIPT_PLAN_ERROR.getCode(), "生成满标资金记录失败");
            }
        }
        // 投资满标生成借款人的还款计划
        try {
            BorrowerBorrowRecord borrowerBorrowRecord = borrowerBorrowRecordDao.getByProductItemId(productItemId);
            if (null == borrowerBorrowRecord) {
                LOGGER.warn("借款人借款记录为空！current productItemId:" + productItemId);
                if (productType != ProductType.YGB) {
                    LOGGER.debug("调试信息--借款人借款记录为空, productItemId:{}", productItemId);
                    throw new OctopusException(OctopusError.DATA_NOT_FOUND.getCode(),
                            "借款人借款记录为空！current productItemId:" + productItemId);
                }
            } else {
                borrowerBorrowRecord.setStatus(BorrowRecordStatus.REPAYMENT);
                Map<String, Object> params = new HashMap<>();
                params.put("id", borrowerBorrowRecord.getId());
                params.put("status", BorrowRecordStatus.REPAYMENT);
                int count = borrowerBorrowRecordDao.update(params);
                if (count != 1) {
                    LOGGER.debug("调试信息--UPDATE_FAIL(更新还款记录状态失败), borrowerBorrowRecordId:{}",
                            borrowerBorrowRecord.getId());
                    throw new OctopusException(OctopusError.UPDATE_FAIL.getCode(), "更新还款记录状态失败");
                }
                String repaymentTypeString = productItemSetting.getInterstCalMode();
                RepaymentType repaymentType = RepaymentType.valueOf(repaymentTypeString);
                CalculationInput input = new CalculationInput();
                input.setAmount(borrowerBorrowRecord.getBorrowAmount());
                Date interestDate = null;
                String interestDatesString = productItemSetting.getInterstStartMode();
                switch (interestDatesString) {
                    case "T0":
                        interestDate = DateUtils.addDays(new Date(), 0);
                        break;
                    case "T1":
                        interestDate = DateUtils.addDays(new Date(), 1);
                        break;
                    case "T2":
                        interestDate = DateUtils.addDays(new Date(), 2);
                        break;
                    default:
                        break;
                }
                input.setInterestDate(interestDate);
                input.setInterestRates(productItemSetting.getYearYield());
                input.setPeriods(productItemSetting.getTerm());
                input.setPeriodType(PeriodType.valueOf(productItemSetting.getTermType()));
                input.setRepaymentType(repaymentType);
                List<CalculationOutput> outputs = Calculator.calculate(input);
                for (CalculationOutput output : outputs) {
                    BorrowerRefundPlan plan = new BorrowerRefundPlan();
                    plan.setBorrowRecordId(borrowerBorrowRecord.getId());
                    plan.setCurrentPeriod(output.getPeroid());
                    plan.setRepaymentAmount(output.getPrincilpeAndInterest());
                    plan.setRepaymentPrincipe(output.getPrinciple());
                    plan.setRepaymentInterest(output.getInterest());
                    plan.setInterestStartTime(DateUtils.addMonths(output.getRepaymentDate(), -1));
                    if ("Once".equals(productItemSetting.getInterstCalMode())) {
                        if("DAY".equals(productItemSetting.getTermType())){
                            plan.setInterestStartTime(DateUtils.addDays(output.getRepaymentDate(), -productItemSetting.getTerm()));
                        }else if("MONTH".equals(productItemSetting.getTermType())){
                            plan.setInterestStartTime(DateUtils.addMonths(output.getRepaymentDate(), -productItemSetting.getTerm()));
                        }
                    }else if ("Month".equals(productItemSetting.getInterstCalMode())) {
                        plan.setInterestStartTime(DateUtils.addMonths(output.getRepaymentDate(), -1));
                    }  
                    plan.setInterestEndTime(output.getRepaymentDate());
                    plan.setStatus(RefundPlanStatus.REPAYMENT);
                    plan.setActualRepaymentAmount(new BigDecimal(0));
                    plan.setActualRepaymentInterest(new BigDecimal(0));
                    borrowerRefundPlanDao.insert(plan);
                    LOGGER.info("生成借款人还款计划成功, BorrowerRefundPlan ID{}", plan.getId());
                }
            }
        } catch (Exception e) {
            LOGGER.error("investmentProductFullTransaction execute failure.", e);
        }
    }
    
    
    @Transactional
    public boolean xpressDedemption(XpressDedemptionIn xpressDedemptionIn) {

        xpressDedemptionValidation(xpressDedemptionIn);
        // 事务执行前日志
            LOGGER.info("随鑫宝-赎回：Method:{},MemberId:{},Time:{},Amount:{},Status:{}",
                        "xpressDedemptionTransaction",
                    xpressDedemptionIn.getMemberId(),
                    getFormatTime(new Date()), xpressDedemptionIn.getAmount()
                            .toString(), "begin");
        // 事务执行
        xpressDedemptionTransaction(xpressDedemptionIn);
        // 事务执行后日志
            LOGGER.info("随鑫宝-赎回：Method:{},MemberId:{},Time:{},Amount:{},Status:{}",
                        "xpressDedemptionTransaction",
                    xpressDedemptionIn.getMemberId(),
                    getFormatTime(new Date()), xpressDedemptionIn.getAmount()
                            .toString(), "end");
        return true;
    }
    /**
     * 校验赎回参数
     * @param xpressDedemptionIn
     * @return
     */
    public boolean xpressDedemptionValidation(XpressDedemptionIn xpressDedemptionIn) {

          String memberId = xpressDedemptionIn.getMemberId();
          BigDecimal amount = xpressDedemptionIn.getAmount();
          String tradePassword = Digest.shaDigest(xpressDedemptionIn.getTradePassword());
          /*String tradePassword = Digest.shaDigest(Digest.md5Digest(xpressDedemptionIn
                  .getTradePassword()));
*/
          // 验证当前用户是否存在
          Member member = memberDao.getById(memberId);
          if (member == null) {
              throw new OctopusException(OctopusError.MEMBER_NOT_EXIST.getCode(), OctopusError.MEMBER_NOT_EXIST.getMessage());
          }
          // 验证交易密码是否正确
          if (tradePassword.equals(member.getTradePassword()) == false) {
              throw new OctopusException(OctopusError.TRADE_PASSWORD_ERROR.getCode(), OctopusError.TRADE_PASSWORD_ERROR.getMessage());
          }
          // 查询会员随鑫宝账户表
          MemberAccountCondition xpressAccountCondition = new MemberAccountCondition();
          xpressAccountCondition.setMemberId(memberId);
          xpressAccountCondition.setType(AccountType.XPRESS.name());
          MemberAccount xpressMemberAccount = memberAccountDao
                  .getMemberAccountByMemberIdAndType(xpressAccountCondition);
          // 赎回金额必须大于零
          if (amount.compareTo(new BigDecimal(0)) <= 0) {
              throw new OctopusException(OctopusError.REDEEM_AMOUNT_MUST_MORE_THAN_ZERO.getCode(), OctopusError.REDEEM_AMOUNT_MUST_MORE_THAN_ZERO.getMessage());
          }
          // 赎回金额的最小单位不能小于0.01元
          if (amount.subtract(amount.setScale(2, BigDecimal.ROUND_DOWN))
                  .compareTo(new BigDecimal(0)) > 0
                  && amount.subtract(amount.setScale(2, BigDecimal.ROUND_DOWN))
                          .compareTo(new BigDecimal(0.01)) < 0) {
              throw new OctopusException(OctopusError.REDEEM_LESS_THAN_PENNY.getCode(), OctopusError.REDEEM_LESS_THAN_PENNY.getMessage());
          }
          // 赎回金额不能大于随鑫宝账户总额
          if (amount.compareTo(xpressMemberAccount.getTotalAmount()) == 1) {
              throw new OctopusException(OctopusError.GREATER_THAN_XPRESS_TOTAL_AMOUNT.getCode(), OctopusError.GREATER_THAN_XPRESS_TOTAL_AMOUNT.getMessage());
          }
          
          
          XpressWhitelist xpressWhitelist = xpressWhitelistDao.getXpressWhitelistByMemberId(memberId);
          if (xpressWhitelist == null) {
              // 查询会员资金记录表（根据账户编号和资金类型）
              MemberFundRecordCondition memberFundRecordCondition = new MemberFundRecordCondition();
              memberFundRecordCondition.setMemberAccountId(xpressMemberAccount
                      .getId());
              // TODO:
              // 资金类型待定
              memberFundRecordCondition.setFundType(FundRecordCategory.REDEEMSFAILURE
                      .name());
              memberFundRecordCondition.setStartDate(getCurrentDate());
              memberFundRecordCondition.setEndDate(addDays(getCurrentDate(), 1));
              /*
               * memberFundRecordCondition.setFundDelayStatus(FundDelayStatus.FREEZE
               * .name());
               */
              List<MemberFundRecord> memberFundRecords = memberFundRecordDao
                      .getMemberFundRecordsByAccountIdAndType(memberFundRecordCondition);
              // 赎回次数限制：每天最多只能赎回一次
              if (memberFundRecords.size() > 0) {
                  throw new OctopusException(OctopusError.EVERYDAY_ONLY_ONCE_REDEEM.getCode(), OctopusError.EVERYDAY_ONLY_ONCE_REDEEM.getMessage());
              }
          } else {
              // 白名单用户判断是否超出单日赎回限额
              // 查询会员资金记录表（根据账户编号和资金类型）
              MemberFundRecordCondition memberFundRecordCondition = new MemberFundRecordCondition();
              memberFundRecordCondition.setMemberAccountId(xpressMemberAccount
                      .getId());
              // TODO:
              // 资金类型待定
              memberFundRecordCondition.setFundType(FundRecordCategory.REDEEMSFAILURE
                      .name());
              memberFundRecordCondition.setStartDate(getCurrentDate());
              memberFundRecordCondition.setEndDate(addDays(getCurrentDate(), 1));
              
              List<MemberFundRecord> memberFundRecords = memberFundRecordDao
                      .getMemberFundRecordsByAccountIdAndType(memberFundRecordCondition);
              
              MemberFundRecordCondition memberFundRecordConditionSuccess = new MemberFundRecordCondition();
              memberFundRecordConditionSuccess.setMemberAccountId(xpressMemberAccount
                      .getId());
              // 查询今天已赎回的随鑫宝资金记录
              memberFundRecordConditionSuccess.setFundType(FundRecordCategory.REDEEMSSUCCESS
                      .name());
              memberFundRecordConditionSuccess.setStartDate(getCurrentDate());
              memberFundRecordConditionSuccess.setEndDate(addDays(getCurrentDate(), 1));
              
              List<MemberFundRecord> memberFundRecordsSuccess = memberFundRecordDao
                      .getMemberFundRecordsByAccountIdAndType(memberFundRecordConditionSuccess);
              memberFundRecords.addAll(memberFundRecordsSuccess);
              // 判断是否超出随鑫宝白名单设置的单日赎回限额
              BigDecimal alreadyDedemptionAmount = new BigDecimal("0.00");
              for (int a = 0; a < memberFundRecords.size(); a++) {
                  alreadyDedemptionAmount = alreadyDedemptionAmount.add(memberFundRecords.get(a).getFundAmount());
              }
              if (amount.add(alreadyDedemptionAmount).compareTo(xpressWhitelist.getDailyRedeemLimit()) > 0) {
                  throw new OctopusException(OctopusError.GREATER_THAN_SXB_SUM_MAX_AMOUNT.getCode(), OctopusError.GREATER_THAN_SXB_SUM_MAX_AMOUNT.getMessage());
              }

          }
          return true;
    }
    
    @Transactional
    public boolean xpressDedemptionTransaction(
            XpressDedemptionIn xpressDedemptionIn) {

        String memberId = xpressDedemptionIn.getMemberId();
        BigDecimal amount = xpressDedemptionIn.getAmount();

        // 查询会员随鑫宝账户表
        MemberAccountCondition xpressAccountCondition = new MemberAccountCondition();
        xpressAccountCondition.setMemberId(memberId);
        xpressAccountCondition.setType(AccountType.XPRESS.name());
        MemberAccount xpressMemberAccount = memberAccountDao
                .getMemberAccountByMemberIdAndType(xpressAccountCondition);
        // 新增随鑫宝账户资金快照
        MemberAccountSnapshot xpressMemberAccountSnapshot = new MemberAccountSnapshot();
        xpressMemberAccountSnapshot.setMemberAccountId(xpressMemberAccount
                .getId());
        xpressMemberAccountSnapshot.setFrozenFund(xpressMemberAccount
                .getFrozenAmount());
        xpressMemberAccountSnapshot.setFundBalance(xpressMemberAccount
                .getAmountBalance());
        xpressMemberAccountSnapshot.setFundBalanceChecksum(xpressMemberAccount
                .getAmountBalanceChecksum());
        xpressMemberAccountSnapshot.setIncommingFund(xpressMemberAccount
                .getIncommingAmount());
        xpressMemberAccountSnapshot.setRechargeFund(xpressMemberAccount
                .getRechargeAmount());
        xpressMemberAccountSnapshot.setTotalFund(xpressMemberAccount
                .getTotalAmount());
        xpressMemberAccountSnapshot.setWithdrawFund(xpressMemberAccount
                .getWithdrawAmount());
        memberAccountSnapshotDao.insertMemberAccountSnapshot(xpressMemberAccountSnapshot);
        // 查询会员主账户表
        MemberAccountCondition memberAccountCondition = new MemberAccountCondition();
        memberAccountCondition.setMemberId(memberId);
        memberAccountCondition.setType(AccountType.PRIMARY.name());
        MemberAccount memberAccount = memberAccountDao
                .getMemberAccountByMemberIdAndType(memberAccountCondition);
        // 新增主账户资金快照
        MemberAccountSnapshot memberAccountSnapshot = new MemberAccountSnapshot();
        memberAccountSnapshot.setMemberAccountId(memberAccount.getId());
        memberAccountSnapshot.setFrozenFund(memberAccount.getFrozenAmount());
        memberAccountSnapshot.setFundBalance(memberAccount.getAmountBalance());
        memberAccountSnapshot.setFundBalanceChecksum(memberAccount
                .getAmountBalanceChecksum());
        memberAccountSnapshot.setIncommingFund(memberAccount
                .getIncommingAmount());
        memberAccountSnapshot
                .setRechargeFund(memberAccount.getRechargeAmount());
        memberAccountSnapshot.setTotalFund(memberAccount.getTotalAmount());
        memberAccountSnapshot
                .setWithdrawFund(memberAccount.getWithdrawAmount());
        memberAccountSnapshotDao.insertMemberAccountSnapshot(memberAccountSnapshot);
        // 增加主账户冻结资金
        /*
         * memberAccount.setFrozenAmount(memberAccount.getFrozenAmount().add(
         * amount)); memberAccountDao.updateMemberAccount(memberAccount);
         */
        // 账户更新条件
        MemberAccountCondition updateAccountCondition = new MemberAccountCondition();
        updateAccountCondition.setId(memberAccount.getId());
        updateAccountCondition.setAmount(amount);
        // 增加主账户冻结资金
        updateAccountCondition.setFrozenAmountFlag(AccountAmountType.INCREASE.name());
        // 更新主账户表
        if (memberAccountDao.updateMemberAccountByCondition(updateAccountCondition) == 0) {
            throw new OctopusException(OctopusError.UPDATE_FAIL.getCode(), "更新账户失败");
        }
        // 计算并更新随鑫宝账户的有效余额
        /*
         * 要判断之前是否有投资冻结记录 若有，则要判断赎回金额与投资冻结金额的大小
         * 若赎回金额小于投资冻结金额，则更新对应的投资冻结记录的冻结金额为（冻结金额-赎回金额）
         * 若赎回金额等于投资冻结金额，则更新对应的投资冻结记录的状态为已解冻
         * 若赎回金额大于投资冻结金额，则更新对应的投资冻结记录的状态为已解冻，更新有效余额为（有效余额-（赎回金额-投资冻结金额））
         * 若无，则更新有效余额为（有效余额-赎回金额）
         */
        // 查询会员资金记录表（根据账户编号和资金类型）
        MemberFundRecordCondition memberFundRecordCondition = new MemberFundRecordCondition();
        memberFundRecordCondition.setMemberAccountId(xpressMemberAccount
                .getId());
        // TODO:
        // 资金类型待定
        memberFundRecordCondition
                .setFundType(FundRecordCategory.FROZENINVESTMENT.name());
        memberFundRecordCondition.setStartDate(getCurrentDate());
        memberFundRecordCondition.setEndDate(addDays(getCurrentDate(), 1));
        memberFundRecordCondition.setFundDelayStatus(FundDelayStatus.ALL_FREEZE
                .name());
        List<MemberFundRecord> memberFundRecords = memberFundRecordDao
                .getMemberFundRecordsByAccountIdAndType(memberFundRecordCondition);
        BigDecimal investFreezeAmount = null;
        BigDecimal balanceAmount = amount;
        String fundDelayStatus = null;
        if (memberFundRecords.size() > 0) {
            // 若有，则要判断赎回金额与投资冻结金额的大小
            for (MemberFundRecord fundRecord : memberFundRecords) {

                // 查询随鑫宝账户对应的投资冻结资金记录
                FundDelay investFundDelay = fundDelayDao
                        .getFundDelayByFundRecordId(fundRecord.getId());
                fundDelayStatus = investFundDelay.getStatus();
                investFreezeAmount = fundRecord.getFundAmount();
                if (balanceAmount.compareTo(new BigDecimal(0)) == 1) {
                    if (balanceAmount.compareTo(investFreezeAmount) == -1) {
                        // 若赎回金额小于投资冻结金额，则更新对应的投资冻结记录的状态为已无效，新增赎回抵消后的对应的投资冻结记录（冻结金额为（冻结金额-赎回金额））
                        investFreezeAmount = investFreezeAmount
                                .subtract(balanceAmount);
                        // 产生和赎回抵消后的新的资金记录
                        MemberFundRecord newFundRecord = new MemberFundRecord();
                        newFundRecord.setMemberId(memberId);
                        newFundRecord.setMemberAccountId(xpressMemberAccount
                                .getId());
                        newFundRecord.setAccountBalance(xpressMemberAccount
                                .getAmountBalance());
                        newFundRecord.setFundAmount(investFreezeAmount);
                        // TODO:
                        newFundRecord.setFundDirection("");
                        newFundRecord.setType(MemberFundRecord.Type.FROZENINVESTMENT);
                        newFundRecord.setFundFrom("");
                        newFundRecord.setFundTo("");
                        LOGGER.info("随鑫宝-抵消投资：MemberId:{},MemberAccountId:{},Time:{},Amount:{}",
                                        memberId,
                                xpressMemberAccount.getId(),
                                getFormatTime(new Date()), amount.toString());
                        newFundRecord.setRemark("随鑫宝-抵消投资");
                        fundRecordDao
                                .insert(newFundRecord);
                        // 产生和赎回抵消后的新的延迟资金记录
                        FundDelay newFundDelay = new FundDelay();
                        newFundDelay.setFundRecordId(newFundRecord.getId());
                        newFundDelay.setStatus(FundDelayStatus.OFFSET_FREEZE
                                .name());
                        newFundDelay.setUnfreezeTime(investFundDelay
                                .getUnfreezeTime());
                        fundDelayDao.insertFundDelay(newFundDelay);
                        balanceAmount = new BigDecimal(0);
                    } else if (balanceAmount.compareTo(investFreezeAmount) == 0) {
                        // 若赎回金额等于投资冻结金额，则更新对应的投资冻结记录的状态为已无效
                        investFreezeAmount = new BigDecimal(0);
                        balanceAmount = new BigDecimal(0);
                    } else {
                        // 若赎回金额大于投资冻结金额，则更新对应的投资冻结记录的状态为已无效，更新有效余额为（有效余额-（赎回金额-投资冻结金额））
                        /* investFreezeAmount = new BigDecimal(0); */
                        balanceAmount = balanceAmount
                                .subtract(investFreezeAmount);
                    }
                    if (fundDelayStatus.equals(FundDelayStatus.FREEZE.name())) {
                        fundDelayStatus = FundDelayStatus.INVALID.name();
                    } else if (fundDelayStatus
                            .equals(FundDelayStatus.OFFSET_FREEZE.name())) {
                        fundDelayStatus = FundDelayStatus.OFFSET_INVALID.name();
                    }
                } else {
                    break;
                }
                investFundDelay.setStatus(fundDelayStatus);
                investFundDelay.setUpdateTime(new Date());
                fundDelayDao.updateFundDelay(investFundDelay);
            }
        } else {
            // 若无，则更新有效余额为（有效余额-赎回金额）
            // 若（有效余额-赎回金额）<=0,则更新有效余额为0
            if (amount.compareTo(xpressMemberAccount.getAmountBalance()) >= 0) {
                balanceAmount = xpressMemberAccount.getAmountBalance();
            }
        }
        // 更新随鑫宝账户的有效余额
        /*
         * xpressMemberAccount.setAmountBalance(xpressMemberAccount
         * .getAmountBalance().subtract(balanceAmount));
         * xpressMemberAccount.setAmountBalanceChecksum(Digest
         * .md5Digest(xpressMemberAccount.getAmountBalance().toString()));
         */
        // 随鑫宝资金账户总额减少为（总额-赎回金额）
        /*
         * xpressMemberAccount.setTotalAmount(xpressMemberAccount.getTotalAmount(
         * ) .subtract(amount));
         * memberAccountDao.updateMemberAccount(xpressMemberAccount);
         */
        // 账户更新条件
        MemberAccountCondition updateXpressAccountCondition = new MemberAccountCondition();
        updateXpressAccountCondition.setId(xpressMemberAccount.getId());
        updateXpressAccountCondition.setAmount(amount);
        updateXpressAccountCondition.setBalanceAmount(balanceAmount);
        // 减少随鑫宝有效余额
        updateXpressAccountCondition
                .setAmountBalanceFlag(AccountAmountType.REDUCE.name());
        // 减少随鑫宝账户总额
        updateXpressAccountCondition
                .setTotalAmountFlag(AccountAmountType.REDUCE.name());
        // 更新对应的账户表
        if (memberAccountDao.updateMemberAccountByXpressCondition(updateXpressAccountCondition) == 0) {
            throw new OctopusException(OctopusError.UPDATE_FAIL.getCode(), "更新随鑫宝账户失败");
        }
        // 查询主账户表
        memberAccount = memberAccountDao
                .getMemberAccountByMemberIdAndType(memberAccountCondition);
        // 产生资金记录
        MemberFundRecord memberFundRecord = new MemberFundRecord();
        memberFundRecord.setMemberId(memberId);
        memberFundRecord.setMemberAccountId(memberAccount.getId());
        memberFundRecord.setAccountBalance(memberAccount.getAmountBalance());
        memberFundRecord.setFundAmount(amount);
        // TODO:
        memberFundRecord.setFundDirection("");
        memberFundRecord
                .setType(MemberFundRecord.Type.REDEEMSFAILURE);
        memberFundRecord.setFundFrom("");
        memberFundRecord.setFundTo("");
        LOGGER.info("随鑫宝-赎回：MemberId:{},Time:{},MemberAccountId:{},Amount:{}",
                        memberId, getFormatTime(new Date()),
                xpressMemberAccount.getId(), amount.toString());
        memberFundRecord.setRemark("随鑫宝-赎回");
        fundRecordDao.insert(memberFundRecord);
        // 查询随鑫宝账户表
        xpressMemberAccount = memberAccountDao
                .getMemberAccountByMemberIdAndType(xpressAccountCondition);
        // 产生随鑫宝的资金记录
        MemberFundRecord xpressFundRecord = new MemberFundRecord();
        xpressFundRecord.setMemberId(memberId);
        xpressFundRecord.setMemberAccountId(xpressMemberAccount.getId());
        xpressFundRecord
                .setAccountBalance(xpressMemberAccount.getTotalAmount());
        xpressFundRecord.setFundAmount(amount);
        // TODO:
        xpressFundRecord.setFundDirection("");
        xpressFundRecord.setType(MemberFundRecord.Type.REDEEMSFAILURE);
        xpressFundRecord.setFundFrom("");
        xpressFundRecord.setFundTo("");
        LOGGER.info("随鑫宝-赎回：MemberId:{},Time:{},MemberAccountId:{},Amount:{}",
                        memberId, getFormatTime(new Date()),
                xpressMemberAccount.getId(), amount.toString());
        xpressFundRecord.setRemark("随鑫宝-赎回");
        fundRecordDao.insert(xpressFundRecord);
        // 添加随鑫宝账户对应的赎回冻结资金记录
        FundDelay fundDelay = new FundDelay();
        // 计算赎回到账日期(13点之前，T+1;13点之后，T+2) 属于随鑫宝白名单的用户是T+0
        Date unfreezeTime = new Date();
        if(xpressWhitelistDao.getXpressWhitelistByMemberId(memberId) == null){
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour < 13) {
                unfreezeTime =  IPPJRCalendar.getNextWorkDay(new Date());
            } else {
                unfreezeTime = IPPJRCalendar.getNextWorkDay(IPPJRCalendar.getNextWorkDay(new Date()));
            }
            fundDelay.setIsWhitelist("N");
        }else{
            fundDelay.setIsWhitelist("Y");
        }
        fundDelay.setUnfreezeTime(unfreezeTime);
        fundDelay.setFundRecordId(xpressFundRecord.getId());
        fundDelay.setStatus(FundDelayStatus.FREEZE.name());
        fundDelayDao.insertFundDelay(fundDelay);

        //平台操作日志
        PlatformOperationLog platformOperationLog = new PlatformOperationLog();
        platformOperationLog.setAmount(amount);
        platformOperationLog.setType(PlatformFundType.REDEEM.name());
        platformOperationLog.setStatus(PlatformFundStatus.FROZEN.name());
        platformOperationLog.setIsExhibition("N");
        platformOperationLog.setOperator(memberId);
        platformOperationLog.setRemark(String.format("客户%s申请赎回随鑫宝%s元", memberId, amount));
        platformOperationLog.setSourceId(fundDelay.getId());
        platformOperationLogDao.insertPlatformOperationLog(platformOperationLog);

        return true;
    }
    
    /**
     * 
     * 时间格式化处理
     * 
     * @param date
     * @return [参数说明]
     * 
     * @return String [返回类型说明]
     */
    public String getFormatTime(Date date) {
        String formatTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatTime = sdf.format(date);
        return formatTime;
    }
    /**
     * 获取当前日期（只含年月日）
     */
    private static Date getCurrentDate() {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        currentDate = calendar.getTime();
        return currentDate;
    }
    /**
     * 
     * 计算指定天数后的时间
     * 
     * @param date 时间
     * @param days 天数
     * @return [参数说明]
     * 
     * @return Date [返回类型说明]
     */
    public Date addDays(Date date, int days) {
        Date newDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        newDate = calendar.getTime();
        return newDate;
    }

    /**
     * 获取计息单位日期
     */
    private static PeriodType getPeriodType(ProductItemSetting productItemSetting, RepaymentType repaymentType) {
        String periodTypeString = productItemSetting.getTermType();
        if (repaymentType == RepaymentType.Once && StringUtils.isBlank(periodTypeString))
            periodTypeString = PeriodType.DAY.name();
        return PeriodType.valueOf(periodTypeString);
    }

    /**
     * 投资中间过程数据缓存
     */
    protected class InvestData {
        private String memberId;
        private String productItemId;
        private BigDecimal amount;
        private ProductType productType;
        private ProductItemSetting productItemSetting;
        private MemberAccount primeAccount;
        private MemberAccount xpressAccount;
        private String productItemName;
        private String isNovice;
        //auto invest
        private String investMode;
        //transfer
        private MemberInvestment originalInvestment;
        private MemberAccount transferAccount;
        private String productCategoryId;
        //加息券ID
        private Long memberCouponId;
        //投资来源
        private Integer clientType;

        public InvestData(String memberId, String productItemId, BigDecimal amount) {
            this.memberId = memberId;
            this.productItemId = productItemId;
            this.amount = amount;
        }

        public MemberAccount getTransferAccount() {
            return transferAccount;
        }

        public void setTransferAccount(MemberAccount transferAccount) {
            this.transferAccount = transferAccount;
        }

        public MemberInvestment getOriginalInvestment() {
            return originalInvestment;
        }

        public void setOriginalInvestment(MemberInvestment originalInvestment) {
            this.originalInvestment = originalInvestment;
        }

        public String getInvestMode() {
            return investMode;
        }

        public void setInvestMode(String investMode) {
            this.investMode = investMode;
        }

        public String getProductItemName() {
            return productItemName;
        }

        public void setProductItemName(String productItemName) {
            this.productItemName = productItemName;
        }

        public String getIsNovice() {
            return isNovice;
        }

        public void setIsNovice(String isNovice) {
            this.isNovice = isNovice;
        }

        public MemberAccount getPrimeAccount() {
            return primeAccount;
        }

        public void setPrimeAccount(MemberAccount primeAccount) {
            this.primeAccount = primeAccount;
        }

        public MemberAccount getXpressAccount() {
            return xpressAccount;
        }

        public void setXpressAccount(MemberAccount xpressAccount) {
            this.xpressAccount = xpressAccount;
        }

        public ProductItemSetting getProductItemSetting() {
            return productItemSetting;
        }

        public void setProductItemSetting(ProductItemSetting productItemSetting) {
            this.productItemSetting = productItemSetting;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getProductItemId() {
            return productItemId;
        }

        public void setProductItemId(String productItemId) {
            this.productItemId = productItemId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public ProductType getProductType() {
            return productType;
        }

        public void setProductType(ProductType productType) {
            this.productType = productType;
        }

        public void setProductCategoryId(String productCategoryId) {
            this.productCategoryId = productCategoryId;
        }

        public String getProductCategoryId() {
            return productCategoryId;
        }

        public Long getMemberCouponId() {
            return memberCouponId;
        }

        public void setMemberCouponId(Long memberCouponId) {
            this.memberCouponId = memberCouponId;
        }

        public Integer getClientType() {
            return clientType;
        }

        public void setClientType(Integer clientType) {
            this.clientType = clientType;
        }
    }

    /**
     * @param investmentId investmentId
     * @return List ReceiptsPlan
     * @throws OctopusException
     */
    public Pagination<ReceiptsPlan> getMemberInvestmentsReceiptsPlan(MultiValueMap<String, String> params) {
        String memberId = params.getFirst(OctopusProperty.memberId.name());
        String investmentId = params.getFirst(OctopusProperty.investmentId.name());
        MemberInvestment investment = memberInvestmentDao.getById(investmentId);
        if(!memberId.equals(investment.getMemberId())){
            throw new OctopusException(OctopusError.INVESTMENT_NOT_BELONG_MEMBER.getCode(), OctopusError.INVESTMENT_NOT_BELONG_MEMBER.getMessage()); 
        }
        Map<String, String> query = new HashMap<>();
        query.put(OctopusProperty.investmentId.name(), investmentId);
        query.put(OctopusProperty.page.name(), params.getFirst(OctopusProperty.page.name()));
        query.put(OctopusProperty.pageSize.name(), params.getFirst(OctopusProperty.pageSize.name()));
        Pagination<ReceiptsPlan> pageVo = new Pagination<>(params, receiptPlanDao.getMemberInvestmentsReceiptsPlanCount(query),
                receiptPlanDao.getMemberInvestmentsReceiptsPlan(query));
        LOGGER.info("investment {} receipts plan size {}", investmentId, pageVo.getTotalSize());
        return pageVo;
    }

    /**
     * 获取用户投资记录统计
     *
     * @param memberId
     * @param productType
     *
     * @return InvestmentTotalVo
     * @
     */
    public InvestmentTotalVo getMemberInvestTotal(String memberId, String productType) throws OctopusException {
        InvestmentTotalVo investmentTotalVo = new InvestmentTotalVo();
        //获取投资总额
        BigDecimal amount = memberInvestmentDao.getMemberInvestAmount(memberId, productType);
        LOGGER.debug("获取投资总额完成");
        investmentTotalVo.setAmount(amount);
        //获取待收利息
        BigDecimal collectInterest = memberInvestmentDao.getMemberInvestCollectInterest(memberId, productType);
        LOGGER.debug("获取待收利息完成");
        investmentTotalVo.setInterestAmount(collectInterest);
        //获取待收本金
        BigDecimal collectPrincipal = memberInvestmentDao.getMemberInvestCollectPrincipal(memberId, productType);
        LOGGER.debug("获取待收本金完成");
        investmentTotalVo.setPrincipalAmount(collectPrincipal);
        //获取累计收益
        BigDecimal sumProfit = memberInvestmentDao.getMemberInvestSumProfit(memberId, productType);
        LOGGER.debug("获取累计收益完成");
        investmentTotalVo.setProfitAmount(sumProfit);
        //获取转入总金额
        BigDecimal inSumAmount = memberInvestmentDao.getMemberTransferInSum(memberId);
        LOGGER.debug("获取转入总金额完成");
        investmentTotalVo.setTransferInAmount(inSumAmount);
        //获取转入笔数
        int inSumCount = memberInvestmentDao.getMemberTransferInCount(memberId);
        LOGGER.debug("获取转入笔数完成");
        investmentTotalVo.setTransferInCount(inSumCount);
        //获取转出总金额
        BigDecimal outSumAmount = memberInvestmentDao.getMemberTransferOutSum(memberId);
        LOGGER.debug("获取转出总金额完成");
        investmentTotalVo.setTransferOutAmount(outSumAmount);
        //获取转出笔数
        int outSumCount = memberInvestmentDao.getMemberTransferOutCount(memberId);
        LOGGER.debug("获取转出笔数完成");
        investmentTotalVo.setTransferOutCount(outSumCount);
        return investmentTotalVo;
    }

    /**
     * 查询投资记录
     * @param params 查询条件
     *               memberId 用户ID
     *               page  页码
     *               pageSize  分页大小
     *               productType 产品类型
     *               status 投资状态
     * @return 分页结果
     * @throws OctopusException
     */
    public Pagination<InvestmentRecordVo> getMemberInvestmentRecord(MultiValueMap<String, String> params) throws OctopusException{
        Map<String, String> query = new HashMap<>();
        String sort = params.getFirst(OctopusProperty.sort.name());
        String status = params.getFirst(OctopusProperty.status.name());
        String productType = params.getFirst(OctopusProperty.productType.name());
        query.put(OctopusProperty.sort.name(), getSortData(sort));
        query.put(OctopusProperty.page.name(), params.getFirst(OctopusProperty.page.name()));
        query.put(OctopusProperty.pageSize.name(), params.getFirst(OctopusProperty.pageSize.name()));
        query.put(OctopusProperty.memberId.name(), params.getFirst(OctopusProperty.memberId.name()));
        query.put(OctopusProperty.status.name(), InvestmentStatus.valueOfName(Integer.parseInt(status)));
        query.put(OctopusProperty.productType.name(), Type.valueOf(Integer.parseInt(productType)).getCode());
        Pagination<InvestmentRecordVo> pageVo = new Pagination<>(params, memberInvestmentDao.getMemberInvestmentRecordCount(query),
                                                        memberInvestmentDao.getMemberInvestmentRecord(query));
        return pageVo;
    }
    
    /**
     * 投资使用加息券的校验
     * @param investData [参数说明]
     * 
     * @return void [返回类型说明]
     */
    public void couponValidate(InvestData investData) {
        Long memberCouponId = investData.getMemberCouponId();
        ProductItemSetting itemSetting = investData.getProductItemSetting();
        BigDecimal amount = investData.getAmount();
        ProductType productType = investData.getProductType();
        String isNovice = investData.getIsNovice();
        String memberId=investData.getMemberId();
        if (memberCouponId == null) {
            return;
        }
        MemberCoupon memberCoupon = memberCouponDao.getMemberCouponById(memberCouponId);
        // 客户加息券非空校验
        if (memberCoupon == null) {
            throw new OctopusException(OctopusError.MEMBER_COUPON_NOT_EXIST.getCode(), OctopusError.MEMBER_COUPON_NOT_EXIST.getMessage());
        }
        // 校验要使用的加息券是否属于当前投资用户
        if (!memberCoupon.getMemberUuid().equals(memberId)) {
            throw new OctopusException(OctopusError.MEMBER_COUPON_NOT_BELONG_MEMBER.getCode(), OctopusError.MEMBER_COUPON_NOT_BELONG_MEMBER.getMessage());
        }
        Coupon coupon = couponDao.getCouponById(memberCoupon.getCouponId());
        // 加息券活动非空校验
        if (coupon == null) {
            throw new OctopusException(OctopusError.COUPON_NOT_EXIST.getCode(), OctopusError.COUPON_NOT_EXIST.getMessage());
        }
        CouponRule couponRule = couponRuleDao.getCouponRuleById(memberCoupon.getCouponRuleId());
        // 加息券规则非空校验
        if (couponRule == null) {
            throw new OctopusException(OctopusError.COUPON_RULE_NOT_EXIST.getCode(), OctopusError.COUPON_RULE_NOT_EXIST.getMessage());
        }
        // 校验适用的标的类别
        boolean supportCurrentType = false;
        switch (productType) {
        case FINANCIAL:
            if (isNovice.equals("Y")) {
                supportCurrentType = couponRule.getSupportNovice();
            } else {
                supportCurrentType = couponRule.getSupportXwy();
            }
            break;
        case PROFIT:
            supportCurrentType = couponRule.getSupportDxb();
            break;
        case INVESTMENT:
            supportCurrentType = couponRule.getSupportJcb();
            break;
        case ANXIN:
            supportCurrentType = couponRule.getSupportZjb();
            break;
        case YGB:
            supportCurrentType = couponRule.getSupportYgb();
            break;
        case PIAOJU:
            supportCurrentType = couponRule.getSupportYhbPj();
            break;
        case AXB:
            supportCurrentType = couponRule.getSupportYhbGq();
            break;
        default:
            break;
        }
        if (!supportCurrentType) {
            throw new OctopusException(OctopusError.COUPON_NOT_SUPPORT_PRODUCT.getCode(), OctopusError.COUPON_NOT_SUPPORT_PRODUCT.getMessage());
        }
        // 校验适用标的期限
        int term = itemSetting.getTerm();
        String termType = itemSetting.getTermType();
        if (termType.equals(TermType.MONTH.name())) {
            if (couponRule.getMinMonth() != null
                    && couponRule.getMinMonth() > 0) {
                if (term < couponRule.getMinMonth()) {
                    throw new OctopusException(OctopusError.PRODUCT_TERM_NOT_COUPON_RULE.getCode(), OctopusError.PRODUCT_TERM_NOT_COUPON_RULE.getMessage());
                }
            }
            if (couponRule.getMaxMonth() != null
                    && couponRule.getMaxMonth() > 0) {
                if (term > couponRule.getMaxMonth()) {
                    throw new OctopusException(OctopusError.PRODUCT_TERM_NOT_COUPON_RULE.getCode(), OctopusError.PRODUCT_TERM_NOT_COUPON_RULE.getMessage());
                }
            }
        } else if (termType.equals(TermType.DAY.name())) {
            if (couponRule.getMinDay() != null && couponRule.getMinDay() > 0) {
                if (term < couponRule.getMinDay()) {
                    throw new OctopusException(OctopusError.PRODUCT_TERM_NOT_COUPON_RULE.getCode(), OctopusError.PRODUCT_TERM_NOT_COUPON_RULE.getMessage());
                }
            }
            if (couponRule.getMaxDay() != null && couponRule.getMaxDay() > 0) {
                if (term > couponRule.getMaxDay()) {
                    throw new OctopusException(OctopusError.PRODUCT_TERM_NOT_COUPON_RULE.getCode(), OctopusError.PRODUCT_TERM_NOT_COUPON_RULE.getMessage());
                }
            }
        }
        // 校验补贴起投金额
        if (couponRule.getMinInvestmentAmount() != null
                && couponRule.getMinInvestmentAmount().compareTo(
                        new BigDecimal(0)) > 0) {
            if (amount.compareTo(couponRule.getMinInvestmentAmount()) < 0) {
                throw new OctopusException(OctopusError.AMOUNT_LESS_THAN_COUPON_MIN_INVEST_AMOUNT.getCode(), OctopusError.AMOUNT_LESS_THAN_COUPON_MIN_INVEST_AMOUNT.getMessage());
            }
        }
        // 校验加息券状态
        MemberCouponStatus memberCouponStatus = memberCoupon.getStatus();
        if (memberCouponStatus != MemberCouponStatus.UNUSED) {
            throw new OctopusException(OctopusError.MEMBER_COUPON_USED.getCode(), OctopusError.MEMBER_COUPON_USED.getMessage());
        }
        // 校验加息券过期时间
        Date expiredTime = memberCoupon.getExpiredTime();
        if (expiredTime.compareTo(new Date()) < 0) {
            throw new OctopusException(OctopusError.MEMBER_COUPON_EXPIRED.getCode(), OctopusError.MEMBER_COUPON_EXPIRED.getMessage());
        }
        // 校验加息金额是否小于0.01￥，若小于，则不能使用此券
        RepaymentType repaymentType = RepaymentType.valueOf(itemSetting
                .getInterstCalMode());
        Date interestDate = calcInterestDate(itemSetting);
        CalculationInput input = new CalculationInput();
        input.setAmount(amount);
        input.setInterestDate(interestDate);
        input.setInterestRates(couponRule.getAnnualYield());
        input.setPeriods(itemSetting.getTerm());
        input.setRepaymentType(RepaymentType.Once);
        input.setPeriodType(getPeriodType(itemSetting, repaymentType));
        List<CalculationOutput> outputs = Calculator.calculate(input);
        CalculationOutput output = outputs.get(0);
        // 校验加息金额是否小于0.01￥，若小于，则不生成收款计划
        BigDecimal interest = output.getInterest();      
        if (interest.subtract(new BigDecimal(0.01))
                .setScale(2, BigDecimal.ROUND_DOWN)
                .compareTo(new BigDecimal(0)) < 0) {
            throw new OctopusException(OctopusError.COUPON_INTEREST_TOO_SAMLL.getCode(), OctopusError.COUPON_INTEREST_TOO_SAMLL.getMessage());
        }
    }
    
    private static Date calcInterestDate(ProductItemSetting productItemSetting){

        Date interestDate = null;
        String interestDatesString = productItemSetting.getInterstStartMode();
        InterestStartMode interestStartMode = InterestStartMode.valueOf(interestDatesString);
        switch (interestStartMode) {
            case T0:
                interestDate = DateUtils.addDays(new Date(), 0);
                break;
            case T1:
                interestDate = DateUtils.addDays(new Date(), 1);
                break;
            case T2:
                interestDate = DateUtils.addDays(new Date(), 2);
                break;
            default:
                break;
        }
        return interestDate;
    }

    /**
     * 投资产品撤销
     * @param params 查询条件
     *               memberId 用户ID
     *               investmentId  投资ID
     * @return
     *              HongBaoRevokeVo  红包撤销
     * @throws
     *              OctopusException
     */
    @Transactional
    public RevokedVo investmentRevoked(MultiValueMap<String, String> params) throws OctopusException{
        // 获取参数
        String memberId = params.getFirst(OctopusProperty.memberId.name());
        String investmentId = params.getFirst(OctopusProperty.investmentId.name());
        // 投资Event
        RevokedVo revokeVo = new RevokedVo();
        // 验证当前用户是否存在
        Member member = memberDao.getById(memberId);
        LOGGER.debug("投资产品-撤销：Method:{},Execute:{},Time:{},Status:{}", "investmentRevoked", "Validate", new Date(), "Begin");
        if (member == null) {
            throw new OctopusException(OctopusError.MEMBER_NOT_FOUND.getCode(), OctopusError.MEMBER_NOT_FOUND.getMessage());
        }
        revokeVo.setMemberId(memberId);
        revokeVo.setMemberType(member.getMemberType());
        // 验证当前投资记录是否存在
        MemberInvestment investment = memberInvestmentDao.getById(investmentId);
        if (investment == null) {
            throw new OctopusException(OctopusError.INVESTMENT_NOT_EXIST.getCode(), OctopusError.INVESTMENT_NOT_EXIST.getMessage());
        }
        revokeVo.setInvestmentId(investmentId);
        // 验证当前投资记录是否属于当前用户
        if (!memberId.equals(investment.getMemberId())) {
            throw new OctopusException(OctopusError.INVESTMENT_NOT_BELONG_MEMBER.getCode(), OctopusError.INVESTMENT_NOT_BELONG_MEMBER.getMessage());
        }
        //验证当前产品项是否存在
        ProductItem productItem = productItemDao.getById(investment.getProductItemId());
        if (productItem == null) {
            throw new OctopusException(OctopusError.PRODUCT_NOT_FOUND.getCode(), OctopusError.PRODUCT_NOT_FOUND.getMessage());
        }
        revokeVo.setProductItemId(productItem.getId());
        revokeVo.setProductType(productItem.getType());
        // 验证产品类型是否正确
        if (!(productItem.getType().equals(ProductType.INVESTMENT) || productItem.getType().equals(ProductType.YGB)
                || productItem.getType().equals(ProductType.ANXIN) || productItem.getType().equals(ProductType.AXB)
                || productItem.getType().equals(ProductType.PIAOJU))) {
            throw new OctopusException(OctopusError.TRADE_TYPE_ERROR.getCode(), OctopusError.TRADE_TYPE_ERROR.getMessage());
        }
        // 验证当前产品项是否处于可撤销状态
        if (!productItem.getStatus().equals(ProductStatus.APPROVED)) {
            throw new OctopusException(OctopusError.INVEST_NOT_REVOKE.getCode(), OctopusError.INVEST_NOT_REVOKE.getMessage());
        }
        // 验证当前时间是否小于发布时间
        if (productItem.getReleaseTime().compareTo(new Date()) > 0) {
            throw new OctopusException(OctopusError.INVEST_NOT_REVOKE.getCode(), OctopusError.INVEST_NOT_REVOKE.getMessage());
        }
        // 查询投资产品额外信息表
        String productItemId = investment.getProductItemId();
        ProductItemInvestInfo prdItemInvestInfo = productItemInvestInfoDao.getByItemId(productItemId);
        // 验证当前产皮是否允许撤标
        ProductType productType = productItem.getType();
        if (prdItemInvestInfo != null && RevokeType.YES.equals(prdItemInvestInfo.getAllowExpired())
                && (productType == ProductType.ANXIN || productType == ProductType.AXB|| productType == ProductType.PIAOJU)
                && prdItemInvestInfo.getExpiredTime().compareTo(new Date()) <= 0)
            throw new OctopusException(OctopusError.INVEST_NOT_REVOKE.getCode(), OctopusError.INVEST_NOT_REVOKE.getMessage());
        // 验证当前时间是否大于流标时间
        if ((productType == ProductType.INVESTMENT || productType == ProductType.YGB)
                && prdItemInvestInfo.getExpiredTime().compareTo(new Date()) <= 0) {
            throw new OctopusException(OctopusError.INVEST_NOT_REVOKE.getCode(), OctopusError.INVEST_NOT_REVOKE.getMessage());
        }
        LOGGER.debug("投资产品-撤销：Method:{},Execute:{},Time:{},Status:{}", "investmentRevoked", "Validate", new Date(), "end");
        LOGGER.debug("投资产品-撤销：Method:{},Execute:{},Time:{},Status:{}", "investmentRevoked", "Revoke", new Date(), "Begin");
        // 查找投资记录
        BigDecimal investmentAmount = investment.getAmount();
        if(investment.getStatus().equals(InvestmentStatus.REVOKED.name())){
            LOGGER.error("该条投资记录已经撤销！memberId={} & ivestmentId={}", memberId, investmentId);
        }
        // 更新投资记录
        investment.setStatus(InvestmentStatus.REVOKED.name());
        Map<String, Object> investParam = new HashMap<>();
        investParam.put(OctopusProperty.id.name(), investmentId);
        investParam.put(OctopusProperty.status.name(), InvestmentStatus.REVOKED.name());
        if(memberInvestmentDao.update(investParam) == 0){
            LOGGER.error("该条投资记录已经撤销！memberId={} & ivestmentId={}", memberId, investmentId);
        }
        // 查找会员资金账户
        MemberAccountCondition memberAccountCondition = new MemberAccountCondition();
        memberAccountCondition.setMemberId(memberId);
        memberAccountCondition.setType(AccountType.PRIMARY.name());
        MemberAccount memberAccount = memberAccountDao.getMemberAccountByMemberIdAndType(memberAccountCondition);
        // 新增账户资金快照
        MemberAccountSnapshot memberAccountSnapshot = new MemberAccountSnapshot();
        memberAccountSnapshot.setMemberAccountId(memberAccount.getId());
        memberAccountSnapshot.setFrozenFund(memberAccount.getFrozenAmount());
        memberAccountSnapshot.setFundBalance(memberAccount.getAmountBalance());
        memberAccountSnapshot.setFundBalanceChecksum(memberAccount.getAmountBalanceChecksum());
        memberAccountSnapshot.setIncommingFund(memberAccount.getIncommingAmount());
        memberAccountSnapshot.setRechargeFund(memberAccount.getRechargeAmount());
        memberAccountSnapshot.setTotalFund(memberAccount.getTotalAmount());
        memberAccountSnapshot.setWithdrawFund(memberAccount.getWithdrawAmount());
        memberAccountSnapshotDao.insertMemberAccountSnapshot(memberAccountSnapshot);
        // 账户更新条件
        MemberAccountCondition updateAccountCondition = new MemberAccountCondition();
        updateAccountCondition.setId(memberAccount.getId());
        updateAccountCondition.setAmount(investmentAmount);
        updateAccountCondition.setAmountBalanceFlag(AccountAmountType.INCREASE.name());
        updateAccountCondition.setFrozenAmountFlag(AccountAmountType.REDUCE.name());
        // 更新主账户表
        if (memberAccountDao.updateMemberAccountByCondition(updateAccountCondition) == 0) {
            throw new OctopusException(OctopusError.MEMBER_ACCOUNT_UPDATE_FAIL.getCode(), OctopusError.MEMBER_ACCOUNT_UPDATE_FAIL.getMessage());
        }
        // 更新收款计划
        /*Map<String, Object> receiptParam = new HashMap<>();
        receiptParam.put(OctopusProperty.investmentId.name(), investmentId);
        receiptParam.put(OctopusProperty.status.name(), ReceiptPlanStatus.REVOKED.name());
        receiptPlanDao.update(receiptParam);*/
        // 更新借款产品项
        Map<String, Object> productParam = new HashMap<>();
        productItem.setCompletedAmount(productItem.getCompletedAmount().subtract(investmentAmount));
        productParam.put(OctopusProperty.id.name(), productItem.getId());
        productParam.put(OctopusProperty.completedAmount.name(), productItem.getCompletedAmount());
        if (productItemDao.update(productParam) == 0) {
            throw new OctopusException(OctopusError.PRODUCT_UPDATE_FAIL.getCode(), OctopusError.PRODUCT_UPDATE_FAIL.getMessage());
        }
        // 查询主账户表
        memberAccount = memberAccountDao.getMemberAccountByMemberIdAndType(memberAccountCondition);
        // 产生资金记录
        MemberFundRecord memberFundRecord = new MemberFundRecord();
        memberFundRecord.setMemberId(memberId);
        memberFundRecord.setMemberAccountId(memberAccount.getId());
        memberFundRecord.setAccountBalance(memberAccount.getAmountBalance());
        memberFundRecord.setFundAmount(investmentAmount);
        memberFundRecord.setType(MemberFundRecord.Type.WITHDRAWINVESTMENT);
        LOGGER.info("投资产品-撤销：MemberId:{},Time:{},ProductItemId:{},Amount:{},InvestmentId:{}",
                memberId, getFormatTime(new Date()), productItem.getId(), investmentAmount.toString(), investment.getId());
        String fundRemark = "投资产品-撤销[<a href=/product/productDetails?productItemId=%s target=_blank >%s</a>]";
        memberFundRecord.setRemark(String.format(fundRemark, productItem.getId(), productItem.getName()));
        memberFundRecord.setProductItemId(productItem.getId());
        memberFundRecord.setInvestMode(investment.getInvestMode());
        fundRecordDao.insert(memberFundRecord);
        // 使用加息券时更新加息券状态
        if (investment.getMemberCouponId() != null){
            // 根据用户加息券ID查询用户加息券
            MemberCouponVo coupon = couponDao.getMemberCouponById(investment.getMemberCouponId());
            // 判断查询状态与前置状态是否一致
            if (coupon == null || !coupon.getStatus().getName().equals(CouponStatus.FROZEN.getName())) {
                throw new OctopusException(OctopusError.MEMBER_COUPON_NO_EXIST.getCode(), OctopusError.MEMBER_COUPON_NO_EXIST.getMessage());
            }
            // 更新用户加息券的状态
            Map<String, Object> couponParam = new HashMap<>();
            couponParam.put(OctopusProperty.id.name(), coupon.getId());
            couponParam.put(OctopusProperty.status.name(), CouponStatus.UNUSED.getId());
            couponDao.updateMemberCouponById(couponParam);
        }
        LOGGER.debug("投资产品-撤销：Method:{},Execute:{},Time:{},Status:{}", "investmentRevoked", "Revoke", new Date(), "end");
        return revokeVo;
    }
    
    /**
     * 记录客户投资对应的平台操作日志
     * @param investData
     * @param investmentId [参数说明]
     * 
     * @return void [返回类型说明]
     */
    public void insertPlatformOperationLog(InvestData investData, String investmentId) {
        ProductType productType = investData.getProductType();
        String productItemName = investData.getProductItemName();
        String memberId = investData.getMemberId();
        BigDecimal amount = investData.getAmount();
        if (productType != ProductType.EXPERIENCE){
            // 新增平台操作日志
            PlatformOperationLog platformOperationLog = new PlatformOperationLog();
            platformOperationLog.setAmount(amount);
            platformOperationLog.setType(PlatformFundType.INVESTMENT.name());
            platformOperationLog.setOperator(memberId);
            platformOperationLog.setRemark(String.format("客户%s投资%s，投资%s元", memberId, productItemName, amount));
            platformOperationLog.setIsExhibition("N");
            platformOperationLog.setSourceId(investmentId);
            switch (productType) {
                case INVESTMENT:
                case ANXIN:
                case AXB:
                case YGB:
                case PIAOJU:
                    platformOperationLog.setStatus(PlatformFundStatus.FROZEN.name());
                    break;
                case XPRESS:
                case FINANCIAL:
                case PROFIT:
                    platformOperationLog.setStatus(PlatformFundStatus.SUCCESS.name());
                    break;
                default:
                    break;
            }
            platformOperationLogDao.insertPlatformOperationLog(platformOperationLog);
        }
    }
    
    /**
     * 使用加息券
     * @param investmentId
     * @param investData [参数说明]
     * 
     * @return void [返回类型说明]
     */
    public void couponUse(String investmentId, InvestData investData) {
        MemberInvestment memberInvestment = memberInvestmentDao.getById(investmentId);
        ProductType productType = investData.getProductType();
        Long memberCouponId = investData.getMemberCouponId();
        if (memberCouponId == null) {
            return;
        }
        MemberCoupon memberCoupon = memberCouponDao.getMemberCouponById(memberCouponId);
        MemberCouponStatus memberCouponStatus = memberCoupon.getStatus();
        // 判断券是否已经被使用
        if (memberCouponStatus != MemberCouponStatus.UNUSED) {
            return;
        }
        // 更新投资记录的加息券ID
        Map<String, Object> params = new HashMap<>();
        params.put("id", investmentId);
        params.put("memberCouponId", memberCouponId);
        memberInvestmentDao.update(params);
        // 更新用户加息券的状态        
        switch (productType) {
            case FINANCIAL:
            case PROFIT:
                memberCouponStatus = MemberCouponStatus.USED;
                break;
            case INVESTMENT:
            case ANXIN:
            case YGB:
            case PIAOJU:
            case AXB:
                memberCouponStatus = MemberCouponStatus.FROZEN;
                break;
            default:
                break;
        }
        memberCoupon.setStatus(memberCouponStatus);
        memberCoupon.setUpdateTime(new Date());
        memberCouponDao.updateMemberCoupon(memberCoupon);
        // TODO:产生新的加息券
    }
    
    /**
     * 更新加息券状态
     * @param memberInvestment
     * @param memberCouponStatus [参数说明]
     * 
     * @return void [返回类型说明]
     */
    public void couponUpdateStatus(MemberInvestment memberInvestment,
            MemberCouponStatus preStatus, MemberCouponStatus status) {
        if (memberInvestment.getMemberCouponId() == null
                || memberInvestment.getMemberCouponId() <= 0) {
            return;
        }
        Long memberCouponId = memberInvestment.getMemberCouponId();
        MemberCoupon memberCoupon = memberCouponDao
                .getMemberCouponById(memberCouponId);
        // 判断查询状态与前置状态是否一致
        if (preStatus != null && memberCoupon.getStatus() != preStatus) {
            return;
        }
        // 更新用户加息券的状态
        memberCoupon.setStatus(status);
        memberCoupon.setUpdateTime(new Date());
        memberCouponDao.updateMemberCoupon(memberCoupon);
    }
    
    /**
     * 生成加息券收款计划
     * @param memberInvestment
     * @param productItemSetting
     * @param memberReceiptPlan [参数说明]
     * 
     * @return void [返回类型说明]
     */
    public void couponReceiptPlan(MemberInvestment memberInvestment,
            ProductItemSetting productItemSetting,
            MemberReceiptPlan memberReceiptPlan) {
        // 准备基础数据
        BigDecimal amount = memberInvestment.getAmount();
        RepaymentType repaymentType = RepaymentType.valueOf(productItemSetting.getInterstCalMode());
        int period = memberReceiptPlan.getPeriod();
        int term = productItemSetting.getTerm();
        // 判断是否是最后一期收款计划
        if (repaymentType.equals(RepaymentType.Month)) {
            if (term == period) {
                period += 1;
            } else {
                return;
            }
        } else if (repaymentType.equals(RepaymentType.Once)) {
            period += 1;
        } else {
            return;
        }
        // 判断当前投资是否使用加息券
        if (memberInvestment.getMemberCouponId() == null) {
            return;
        }
        MemberCoupon memberCoupon = memberCouponDao.getMemberCouponById(memberInvestment.getMemberCouponId());
        // 判断使用的加息券是否存在
        if (memberCoupon == null) {
            return;
        }
        CouponRule couponRule = couponRuleDao.getCouponRuleById(memberCoupon.getCouponRuleId());
        // 投资金额与补贴投资上限进行比较,若投资金额大于补贴投资上限，则投资金额=补贴投资上限
        if (couponRule.getMaxInvestmentAmount() != null
                && couponRule.getMaxInvestmentAmount().compareTo(
                        new BigDecimal(0)) > 0) {
            if (amount.compareTo(couponRule.getMaxInvestmentAmount()) > 0) {
                amount = couponRule.getMaxInvestmentAmount();
            }
        }
        // 计算加息券获得的利息
        Date interestDate = calcInterestDate(productItemSetting);
        CalculationInput input = new CalculationInput();
        input.setAmount(amount);
        input.setInterestDate(interestDate);
        input.setInterestRates(couponRule.getAnnualYield());
        input.setPeriods(productItemSetting.getTerm());
        input.setRepaymentType(RepaymentType.Once);
        input.setPeriodType(getPeriodType(productItemSetting, repaymentType));
        List<CalculationOutput> outputs = Calculator.calculate(input);
        CalculationOutput output = outputs.get(0);
        // 校验加息金额是否小于0.01￥，若小于，则不生成收款计划
        BigDecimal interest = output.getInterest();
        if (interest.subtract(new BigDecimal(0.01)).setScale(2, BigDecimal.ROUND_DOWN).compareTo(new BigDecimal(0)) < 0) {
            return;
        }
        // 生成加息券产生的收款计划
        MemberReceiptPlan receiptPlan = new MemberReceiptPlan();
        receiptPlan = memberReceiptPlan;
        receiptPlan.setPrincipeAmount(new BigDecimal(0));        
        receiptPlan.setInterestAmount(interest);
        receiptPlan.setSettlementAmount(interest);
        receiptPlan.setPeriod(period);
        receiptPlan.setCouponProfit(true);        
        receiptPlanDao.insert(receiptPlan);
    }  
    /**
     * 获取白名单
     * @param memberId
     * @return
     */
    public XpressWhitelist getXpressWhitelistByMemberId(String memberId){
    	  XpressWhitelist xpressWhitelist = xpressWhitelistDao.getXpressWhitelistByMemberId(memberId);
    	  return xpressWhitelist;
    }
    
    /**
     * 投资状态统计
     * @param params
     * @return [参数说明]
     * 
     * @return List<InvestmentStatusVo> [返回类型说明]
     */
    public List<InvestmentStatusVo> getInvestmentStatusTotal( MultiValueMap<String, String> params){
        Map<String, String> query = new HashMap<>();
        String productType = params.getFirst(OctopusProperty.productType.name());
        query.put(OctopusProperty.memberId.name(), params.getFirst(OctopusProperty.memberId.name()));
        query.put(OctopusProperty.status.name(), params.getFirst(OctopusProperty.status.name()));
        query.put(OctopusProperty.productType.name(), Type.valueOf(Integer.parseInt(productType)).getCode());
        return memberInvestmentDao.getInvestmentStatusTotal(query);
    }
}
    

