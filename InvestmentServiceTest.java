
eckage com.unfae.octopus.service;

import com.ippjr.common.calculator.CalculationInput;
import com.ippjr.common.calculator.CalculationOutput;
import com.ippjr.common.calculator.Calculator;
import com.ippjr.common.calculator.RepaymentType;
import com.ippjr.utils.encrypt.Digest;
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
import com.unfae.octopus.dao.SnapshotDao;
import com.unfae.octopus.dao.XpressWhitelistDao;
import com.unfae.octopus.dao.condition.MemberAccountCondition;
import com.unfae.octopus.dao.condition.MemberFundRecordCondition;
import com.unfae.octopus.entity.Coupon;
import com.unfae.octopus.entity.CouponRule;
import com.unfae.octopus.entity.Member;
import com.unfae.octopus.entity.MemberAccount;
import com.unfae.octopus.entity.MemberAccountSnapshot;
import com.unfae.octopus.entity.MemberCoupon;
import com.unfae.octopus.entity.MemberFundRecord;
import com.unfae.octopus.entity.MemberInvestment;
import com.unfae.octopus.entity.MemberReceiptPlan;
import com.unfae.octopus.entity.PlatformOperationLog;
import com.unfae.octopus.entity.Product;
import com.unfae.octopus.entity.ProductCategorySetting;
import com.unfae.octopus.entity.ProductItem;
import com.unfae.octopus.entity.ProductItemInvestInfo;
import com.unfae.octopus.entity.ProductItemSetting;
import com.unfae.octopus.entity.XpressDedemptionIn;
import com.unfae.octopus.entity.XpressWhitelist;
import com.unfae.octopus.enums.AccountAmountType;
import com.unfae.octopus.enums.AccountType;
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
import com.unfae.octopus.enums.TermType;
import com.unfae.octopus.enums.TrueFalseType;
import com.unfae.octopus.enums.Type;
import com.unfae.octopus.model.ClientType;
import com.unfae.octopus.util.DateUtils;
import com.unfae.octopus.vo.InvestResult;
import com.unfae.octopus.vo.InvestmentRecordVo;
import com.unfae.octopus.vo.InvestmentTotalVo;
import com.unfae.octopus.vo.MemberInvestmentsOverview;
import com.unfae.octopus.vo.RevokedVo;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mockit.Deencapsulation.invoke;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by yangzhichao on 15/11/4.
 */
@RunWith(JMockit.class)
public class InvestmentServiceTest {

    @Tested
    private InvestmentService investmentService;

    @Injectable
    private SnapshotDao snapshotDao;

    @Injectable
    private MemberInvestmentDao memberInvestmentDao;
    @Injectable
    private MemberDao memberDao;
    @Injectable
    private ProductItemDao productItemDao;
    @Injectable
    private ProductDao productDao;
    @Injectable
    private ProductItemSettingDao productItemSettingDao;
    @Injectable
    private ProductItemInvestInfoDao productItemInvestInfoDao;
    @Injectable
    private MemberAccountDao memberAccountDao;
    @Injectable
    private FundRecordDao fundRecordDao;
    @Injectable
    private FundDelayDao fundDelayDao;
    @Injectable
    private PlatformAccountDao platformAccountDao;
    @Injectable
    private ReceiptPlanDao receiptPlanDao;
    @Injectable
    private BorrowerBorrowRecordDao borrowerBorrowRecordDao;
    @Injectable
    private BorrowerRefundPlanDao borrowerRefundPlanDao;
    @Injectable
    private XpressWhitelistDao xpressWhitelistDao;
    @Injectable
    private ProductCategorySettingDao productCategorySettingDao;
    @Injectable
    private ProductItemTransferInfoDao productItemTransferInfoDao;
    @Injectable
    private MemberFundRecordDao memberFundRecordDao;
    @Injectable
    private MemberAccountSnapshotDao memberAccountSnapshotDao;
    @Injectable
    private PlatformOperationLogDao platformOperationLogDao;
    @Injectable
    private MemberCouponDao memberCouponDao;
    @Injectable
    private CouponDao couponDao;
    @Injectable
    private CouponRuleDao couponRuleDao;


    @Test
    public void testGetMemberInvestmentsOverview(){

        /* 初始化 */
        final String memberId = "member001";

        final List<MemberInvestmentsOverview> investmentsOverviews = new ArrayList<>();
        MemberInvestmentsOverview memberInvestmentsOverview = new MemberInvestmentsOverview();
        memberInvestmentsOverview.setTotalAmount(new BigDecimal(8888.6666));
        memberInvestmentsOverview.setAverageRate(new BigDecimal(12.12345));
        memberInvestmentsOverview.setType(AccountType.PRIMARY.name());
        investmentsOverviews.add(memberInvestmentsOverview);

        new Expectations(){
            {
                memberInvestmentDao.getMemberInvestmentsOverview(memberId);returns(investmentsOverviews);
            }
        };

        /* 执行 */
        Map<String, MemberInvestmentsOverview> overviewMap = investmentService.getMemberInvestmentsOverview(memberId);

        /* 验证处理小数位数的逻辑 */
        assertTrue(overviewMap.get(AccountType.PRIMARY.name()).getAverageRate().doubleValue() == 12.1234);
        assertTrue(overviewMap.get(AccountType.PRIMARY.name()).getTotalAmount().doubleValue()==8888.66);

    }

    /**
     * 各个测试方法中多次会用到,这里统一构造
     * @return
     */
    private static MultiValueMap<String,String> buildMockParams(){
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(OctopusProperty.memberId.name(), "member001");
        params.add(OctopusProperty.productUuid.name(),"productUuid001");
        params.add(OctopusProperty.amount.name(),"800");
        params.add(OctopusProperty.tradePassword.name(),"admin");
        params.add(OctopusProperty.investMode.name(), InvestMode.MANUAL.name());
        params.add(OctopusProperty.couponId.name(), "1001");
        params.add(OctopusProperty.clientType.name(), String.valueOf(ClientType.WEB.getId()));
        return params;
    }

    /**
     * 各个测试方法中多次会用到,这里统一构造
     * @return
     */
    private static InvestmentService.InvestData buildMockInvestData(){
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amount = new BigDecimal(1000);
        final InvestmentService.InvestData investData = new InvestmentService().new InvestData(memberId,productItemId,amount);
        return investData;
    }

    @Test
    public void testInvest(){

        /* 初始化准备数据 */
        final MultiValueMap<String, String> params = buildMockParams();

        final InvestmentService.InvestData investData = buildMockInvestData();

        final String memberId = params.getFirst(OctopusProperty.memberId.name());
        final String productItemId = params.getFirst(OctopusProperty.productUuid.name());
        final BigDecimal amount = new BigDecimal(params.getFirst(OctopusProperty.amount.name()));
        final String tradePassword = params.getFirst(OctopusProperty.tradePassword.name());
        final String investMode = params.getFirst(OctopusProperty.investMode.name());
        final Long memberCouponId = Long.parseLong(params.getFirst(OctopusProperty.couponId.name()));

        final InvestResult investResult = new InvestResult();

        /** (关键点总结1)这个地方要注意,传入了InvestmentService.class,在下方调用私有方法时需要传入此参数 **/
        new Expectations(InvestmentService.class){
            {
                /** (关键点总结2)方法内部调用的两个私有方法,这里全部mock掉,这样程序就不会走方法的内部逻辑;我们假设这两个方法的单元测试已经通过 **/
                /** (关键点总结2)对于私有方法的调用,需要用invoke的方式调用 **/
                invoke(investmentService,"prepareAndValidInvestData",memberId, productItemId, amount, tradePassword, investMode, memberCouponId);result=investData;
                invoke(investmentService,"investTransaction",investData);result=investResult;

            }
        };

        /* 执行 */
        investmentService.invest(params);

        /* 验证预期结果 */
        assertEquals(investData.getInvestMode(),investMode);
        Integer clientType = Integer.parseInt(params.getFirst(OctopusProperty.clientType.name()));
        assertEquals(investData.getClientType(),clientType);
        assertEquals(investResult.getMemberId(),memberId);

    }

    @Test
    public void testPrepareAndValidInvestData(){
        /* 初始化 */
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amountParam = new BigDecimal(8000);
        String investMode = InvestMode.MANUAL.name();
        String tradePassword = "123456";
        Long memberCouponId = 66668888L;

        // mock Member对象
        final Member member = new Member();
        member.setId(memberId);
        member.setTradePassword(Digest.shaDigest(tradePassword));

        // mock ProductItem对象
        final ProductItem productItem = new ProductItem();
        productItem.setId(productItemId);
        productItem.setProductId("product001");
        productItem.setProductCategoryId("productCategory001");
        productItem.setItemSettingId("itemSetting001");
        productItem.setType(ProductType.FINANCIAL);
        productItem.setName(ProductType.FINANCIAL.getValue());
        productItem.setIsNovice(TrueFalseType.Y.name());
        // 产品状态
        productItem.setStatus(ProductStatus.APPROVED);
        productItem.setAmount(new BigDecimal(90000));
        productItem.setCompletedAmount(new BigDecimal(6000));
        // 设置产品发布日期为今天以前
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH,-1);
        productItem.setReleaseTime(calendar.getTime());

        // mock Product对象
        final Product product = new Product();
        //product.setType(ProductType.TRANSFER.name());
        //product.setType(ProductType.FINANCIAL.name());

        // mock ProductItemSetting对象
        final ProductItemSetting productItemSetting = new ProductItemSetting();
        productItemSetting.setId(productItem.getItemSettingId());
        // 设置最小投资金额为100元
        productItemSetting.setMinInvestmentAmount(new BigDecimal(100));

        // mock ProductCategorySetting对象
        final ProductCategorySetting productCategorySetting = new ProductCategorySetting();

        // mock ProductItemInvestInfo对象
        final ProductItemInvestInfo productItemInvestInfo = new ProductItemInvestInfo();
        calendar.add(Calendar.DAY_OF_MONTH,3);
        productItemInvestInfo.setExpiredTime(calendar.getTime());

        new Expectations(InvestmentService.class){
            {
                //mock预期执行结果,结果赋值为mock的对象

                // 设置产品类型为体验金
                product.setType(ProductType.EXPERIENCE.name());
                memberDao.getById(memberId);result=member;
                productItemDao.getById(productItemId);result=productItem;
                productDao.getProduct(productItem.getProductId());result=product;
                productItemSettingDao.getById(productItem.getItemSettingId());result=productItemSetting;
                productCategorySettingDao.getByCategoryIdAndProperty(productItem.getProductCategoryId(),
                        ProductDictProperty.CompanyInvestEnabled.name());result=productCategorySetting;
            }
            /*{
                // 设置产品类型为INVESTMENT
                product.setType(ProductType.INVESTMENT.name());
                productItemInvestInfoDao.getByItemId(productItemId);
                memberDao.getById(memberId);result=member;
                productItemDao.getById(productItemId);result=productItem;
                productDao.getProduct(productItem.getProductId());result=product;
                productItemSettingDao.getById(productItem.getItemSettingId());result=productItemSetting;
                productCategorySettingDao.getByCategoryIdAndProperty(productItem.getProductCategoryId(),
                        ProductDictProperty.CompanyInvestEnabled.name());result=productCategorySetting;
                productItemInvestInfoDao.getByItemId(productItemId);result=productItemInvestInfo;

            }*/
        };

        /* 执行 */
        InvestmentService.InvestData investData = invoke(investmentService,"prepareAndValidInvestData",memberId, productItemId, amountParam, tradePassword, investMode, memberCouponId);

        assertEquals(investData.getProductType(),productItem.getType());
        assertEquals(investData.getProductItemName(),productItem.getName());
        assertEquals(investData.getIsNovice(),productItem.getIsNovice());
        assertEquals(investData.getProductCategoryId(),productItem.getProductCategoryId());
        assertEquals(investData.getProductItemSetting().getId(),productItemSetting.getId());



        /* 验证 */
//        new Verifications(){
//            {
//
//            }
//        };

    }



    @Test
    public void testXpressMaxInvestmentAmountCheck(){
        /* 初始化 */
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amount = new BigDecimal(1000);
        final InvestmentService.InvestData investData = investmentService.new InvestData(memberId,productItemId,amount);
        // productItemSetting
        final ProductItemSetting productItemSetting = new ProductItemSetting();
        productItemSetting.setMaxInvestmentAmount(new BigDecimal(8000));
        investData.setProductItemSetting(productItemSetting);
        // xpressAcount
        final MemberAccount memberAcount = new MemberAccount();
        memberAcount.setTotalAmount(new BigDecimal(2000));
        investData.setXpressAccount(memberAcount);

        // 白名单
        final XpressWhitelist xpressWhitelist = new XpressWhitelist();

        // 两种情况的判断
        new Expectations(InvestmentService.class){
            {
                xpressWhitelist.setPurchaseLimit(new BigDecimal(6000));
                /** (关键点总结3)一次mock调用,模拟返回两个结果,指定times为2次,是对应执行阶段的两次方法执行 **/
                xpressWhitelistDao.getXpressWhitelistByMemberId(investData.getMemberId());
                result=xpressWhitelist;
                result=null;
                times=2;
            }
        };

        /* 执行 */
        boolean result = invoke(investmentService,"xpressMaxInvestmentAmountCheck",investData);
        assertEquals(result, true);
        boolean result2 = invoke(investmentService,"xpressMaxInvestmentAmountCheck",investData);
        assertEquals(result2,true);

        /** (关键点总结4)也可在同一个测试方法内,写多个场景下的测试,因为我们只需要遵循Record-Replay-Verify模型,至于在一个方法内写多少种场景测试都没关系 **/
        /* 验证结果为false情况 */
        new Expectations(){
            {
                xpressWhitelist.setPurchaseLimit(new BigDecimal(2500));
                xpressWhitelistDao.getXpressWhitelistByMemberId(investData.getMemberId());
                result=xpressWhitelist;
            }
        };
        boolean result3 = invoke(investmentService,"xpressMaxInvestmentAmountCheck",investData);
        assertEquals(result3, false);


    }

    @Test
    public void testUpdateProductItem(){
        /* 初始化 */
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amount = new BigDecimal(8000);
        final ProductItemSetting productItemSetting = new ProductItemSetting();
        productItemSetting.setMinInvestmentAmount(new BigDecimal(1));
        final InvestmentService.InvestData investData = investmentService.new InvestData(memberId,productItemId,amount);
        investData.setProductItemSetting(productItemSetting);
        investData.setProductType(ProductType.XPRESS);
        final ProductItem productItem = new ProductItem();
        productItem.setAmount(new BigDecimal(1000));
        productItem.setCompletedAmount(new BigDecimal(1000));

        new Expectations(InvestmentService.class){
            {
                /** (关键点总结5)在执行阶段,如果想匹配上本次mock方法的行为,前提是传入方法的参数数量及参数值必须和运行时的方法入参完全匹配 **/
                productItemDao.updateCompletedAmount(productItemId, amount, productItemSetting.getMinInvestmentAmount());result=1;
                /** (关键点总结6)注意,这里将第一个参数设置为anyString,在执行阶段同样可以匹配到此mock方法 **/
                //productItemDao.updateCompletedAmount(anyString, amount, productItemSetting.getMinInvestmentAmount());result=1;
                invoke(investmentService, "xpressMaxInvestmentAmountCheck", investData);result=true;
                productItemDao.getById(productItemId);
                result=productItem;
                /** 以下两种方式都可以实现mock **/
                // 方式1
                // Map<String, Object> params = withCapture(new ArrayList<Map<String, Object>>());
                // System.out.println(params);
                // productItemDao.update(params);result=1;
                // 方式2
                productItemDao.update((Map<String, Object>) any);result=1;
            }
        };
        /* 执行 */
        boolean result = invoke(investmentService,"updateProductItem",investData);
        /* 验证 */
        assertTrue(result);
        new Verifications(){
            {
                Map<String, Object> params = null;
                /** (关键点总结7)验证阶段常用用法,在验证阶段,通过withCapture方法,可以捕获到在运行期,传入到mock方法行为中的参数,这样就可以验证一些过程数据 **/
                productItemDao.update(params = withCapture());
                assertEquals(params.get("status"),ProductStatus.REPAYMENT);
            }
        };

    }

    @Test
    public void testAccuntSnapshot(){

        /* 初始化 */
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amount = new BigDecimal(1000);
        final InvestmentService.InvestData investData = investmentService.new InvestData(memberId,productItemId,amount);
        investData.setProductType(ProductType.XPRESS);
        MemberAccount primeAccount = new MemberAccount();
        investData.setPrimeAccount(primeAccount);
        MemberAccount xpressAccount = new MemberAccount();
        investData.setXpressAccount(xpressAccount);

        new Expectations(InvestmentService.class){
            {
                invoke(investmentService, "createAccountSnapshot", investData.getPrimeAccount());result="mock1";
                invoke(investmentService, "createAccountSnapshot", investData.getXpressAccount());result="mock2";
            }
        };

        /* 执行 */
        invoke(investmentService, "accountSnapshot", investData);

    }


    @Test
    public void testUpdateAccount(){

        /* 初始化 */
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amount = new BigDecimal(1000);
        final InvestmentService.InvestData investData = investmentService.new InvestData(memberId,productItemId,amount);
        final ProductType productType = ProductType.XPRESS;
        investData.setProductType(productType);
        final MemberAccount primeAccount = new MemberAccount();
        primeAccount.setId("primeAccount001");
        investData.setPrimeAccount(primeAccount);
        final MemberAccount resultAccount = new MemberAccount();
        resultAccount.setTotalAmount(new BigDecimal(888));
        final MemberAccount xpressAccount = new MemberAccount();
        xpressAccount.setId("xpressAccount001");
        investData.setXpressAccount(xpressAccount);


        new Expectations(InvestmentService.class){
            {
                memberAccountDao.updatePrimeAccountOnInvesting(primeAccount.getId(), amount, false);result=1;
                memberAccountDao.getByMemberIdAndType(investData.getMemberId(), AccountType.PRIMARY.name());result=resultAccount;
                memberAccountDao.getByMemberIdAndType(xpressAccount.getMemberId(), AccountType.XPRESS.name());result=xpressAccount;
                memberAccountDao.updateXpressAccountOnInvesting(xpressAccount.getId(), amount);result=1;
            }
        };

        /* 执行 */
        invoke(investmentService, "updateAccount", investData);
        /* 验证 */
        assertEquals(investData.getPrimeAccount().getTotalAmount(), new BigDecimal(888));

    }

    @Test
    public void testInsertInvestment_Investment(){

        /* 初始化 */
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amount = new BigDecimal(1000);
        final InvestmentService.InvestData investData = investmentService.new InvestData(memberId,productItemId,amount);
        investData.setProductType(ProductType.XPRESS);
        investData.setClientType(ClientType.ANDROID.getId());

        /* 执行 */
        invoke(investmentService, "insertInvestment", investData);
        /* 验证 */
        new Verifications(){
            {
                MemberInvestment memberInvestment = null;
                memberInvestmentDao.insert(memberInvestment = withCapture());
                assertEquals(memberInvestment.getStatus(), InvestmentStatus.INVESTING.name());
                assertEquals(memberInvestment.getInvestMode(), InvestMode.MANUAL.name());
                assertEquals(memberInvestment.getMemberId(), memberId);
                assertEquals(memberInvestment.getProductItemId(), productItemId);
                assertEquals(memberInvestment.getAmount(), amount);
                assertEquals(memberInvestment.getClientType(), investData.getClientType());
            }
        };

    }

    @Test
    public void testInsertInvestment_Financial(){

        /* 初始化 */
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amount = new BigDecimal(1000);
        final InvestmentService.InvestData investData = investmentService.new InvestData(memberId,productItemId,amount);
        investData.setProductType(ProductType.FINANCIAL);
        investData.setClientType(ClientType.ANDROID.getId());

        /* 执行 */
        invoke(investmentService, "insertInvestment", investData);
        /* 验证 */
        new Verifications(){
            {
                MemberInvestment memberInvestment = null;
                memberInvestmentDao.insert(memberInvestment = withCapture());
                assertEquals(memberInvestment.getStatus(), InvestmentStatus.RECEIPTING.name());
                assertEquals(memberInvestment.getInvestMode(), InvestMode.MANUAL.name());
                assertEquals(memberInvestment.getMemberId(), memberId);
                assertEquals(memberInvestment.getProductItemId(), productItemId);
                assertEquals(memberInvestment.getAmount(), amount);
                assertEquals(memberInvestment.getClientType(), investData.getClientType());
            }
        };

    }

    @Test
    public void testInsertFundRecord_FINANCIAL(){

        /* 初始化 */
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amount = new BigDecimal(1000);
        final InvestmentService.InvestData investData = investmentService.new InvestData(memberId,productItemId,amount);
        investData.setProductType(ProductType.FINANCIAL);
        investData.setClientType(ClientType.ANDROID.getId());

        final MemberAccount primeAccount = new MemberAccount();
        primeAccount.setId("primeAccount001");
        primeAccount.setAmountBalance(new BigDecimal(100));
        investData.setPrimeAccount(primeAccount);
        final MemberAccount xpressAccount = new MemberAccount();
        xpressAccount.setId("xpressAccount001");
        investData.setXpressAccount(xpressAccount);

        final String investmentId = "investment001";

        /* 执行 */
        invoke(investmentService, "insertFundRecord", investData, investmentId);
        /* 验证 */
        new Verifications(){
            {
                MemberFundRecord memberFundRecord = null;
                fundRecordDao.insert(memberFundRecord = withCapture());
                assertEquals(memberFundRecord.getMemberId(), memberId);
                assertEquals(memberFundRecord.getMemberAccountId(), primeAccount.getId());
                assertEquals(memberFundRecord.getAccountBalance(), primeAccount.getAmountBalance());
                assertEquals(memberFundRecord.getFundAmount(), amount);
                assertEquals(memberFundRecord.getInvestmentId(), investmentId);
                assertEquals(memberFundRecord.getProductItemId(),productItemId);
                assertEquals(memberFundRecord.getClientType(),investData.getClientType());
                assertEquals(memberFundRecord.getType(),MemberFundRecord.Type.SUCCESSINVESTMENT);

            }
        };

    }

    @Test
    public void testUpdateAllocateAccount_FINANCIAL(){

        /* 初始化 */
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amount = new BigDecimal(1000);
        final InvestmentService.InvestData investData = investmentService.new InvestData(memberId,productItemId,amount);
        investData.setProductType(ProductType.FINANCIAL);

        new Expectations(){
            {
                platformAccountDao.updateInvest(amount);result=1;
            }
        };

        /* 执行 */
        invoke(investmentService, "updateAllocateAccount", investData);

        /* 验证 */

    }

    @Test
    public void testUpdateAllocateAccount_XPRESS(){

        /* 初始化 */
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amount = new BigDecimal(1000);
        final InvestmentService.InvestData investData = investmentService.new InvestData(memberId,productItemId,amount);
        investData.setProductType(ProductType.XPRESS);

        final XpressWhitelist xpressWhitelist = new XpressWhitelist();

        new Expectations(){
            {
                xpressWhitelistDao.getXpressWhitelistByMemberId(investData.getMemberId());result=xpressWhitelist;
                platformAccountDao.updateInvestXpress(new BigDecimal(0), amount.setScale(6, BigDecimal.ROUND_HALF_EVEN));result=1;
            }
        };

        /* 执行 */
        invoke(investmentService, "updateAllocateAccount", investData);

        /* 验证 */
        /** 注意,此处验证块完全可以不用写,因为,主要验证的逻辑就是检验一下入参.按照参数匹配原则,在上面的Expectations块中已经完全可以匹配**/
        new Verifications(){
            {
                BigDecimal num1 = null;
                BigDecimal num2 = null;
                platformAccountDao.updateInvestXpress(num1=withCapture(),num2=withCapture());
                assertEquals(num1, new BigDecimal(0));
                assertEquals(num2,amount.setScale(6, BigDecimal.ROUND_HALF_EVEN));
            }
        };

    }

    @Test
    public void testInsertPIlatformOperationLog(){

        /* 初始化 */
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amount = new BigDecimal(1000);
        final InvestmentService.InvestData investData = investmentService.new InvestData(memberId,productItemId,amount);
        investData.setProductType(ProductType.XPRESS);
        investData.setProductItemName("productItemName001");
        final String investmentId = "investment001";

        /* 执行 */
        invoke(investmentService, "insertPlatformOperationLog", investData, investmentId);

        /* 验证 */
        new Verifications(){
            {
                PlatformOperationLog platformOperationLog = null;
                platformOperationLogDao.insertPlatformOperationLog(platformOperationLog = withCapture());
                assertEquals(platformOperationLog.getAmount(), amount);
                assertEquals(platformOperationLog.getType(), PlatformOperationLog.PlatformFundType.INVESTMENT.name());
                assertEquals(platformOperationLog.getOperator(), memberId);
                assertEquals(platformOperationLog.getRemark(), String.format("客户%s投资%s，投资%s元", memberId, investData.getProductItemName(), amount));
                assertEquals(platformOperationLog.getIsExhibition(), "N");
                assertEquals(platformOperationLog.getSourceId(), investmentId);
                assertEquals(platformOperationLog.getAmount(), amount);
                assertEquals(platformOperationLog.getStatus(), PlatformOperationLog.PlatformFundStatus.SUCCESS.name());

            }
        };

    }

    @Test
    public void testCouponUse(){

        /* 初始化 */
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amount = new BigDecimal(1000);
        final InvestmentService.InvestData investData = investmentService.new InvestData(memberId,productItemId,amount);
        investData.setProductType(ProductType.FINANCIAL);
        investData.setMemberCouponId(1001L);

        final String investmentId = "investment001";

        final MemberInvestment memberInvestment = new MemberInvestment();
        final MemberCoupon memberCoupon = new MemberCoupon();
        memberCoupon.setStatus(MemberCouponStatus.UNUSED);

        new Expectations(){
            {
                memberInvestmentDao.getById(investmentId);result=memberInvestment;
                memberCouponDao.getMemberCouponById(investData.getMemberCouponId());result=memberCoupon;
                memberInvestmentDao.update((Map<String, Object>)any);
                memberCouponDao.updateMemberCoupon((MemberCoupon) any);
            }
        };

        /* 执行 */
        invoke(investmentService, "couponUse",investmentId, investData);

        /* 验证 */
        new Verifications(){
            {
                Map<String, Object> params = null;
                memberInvestmentDao.update(params = withCapture());
                assertEquals(params.get("id"), investmentId);
                assertEquals(params.get("memberCouponId"), investData.getMemberCouponId());

                MemberCoupon memberCoupon1 = null;
                memberCouponDao.updateMemberCoupon(memberCoupon1 = withCapture());
                assertEquals(memberCoupon1.getStatus(),MemberCouponStatus.USED);

            }
        };

    }


    @Test
    public void testInvestTransaction() {

        /* 初始化 */
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amount = new BigDecimal(1000);
        final InvestmentService.InvestData investData = investmentService.new InvestData(memberId, productItemId, amount);
        investData.setProductType(ProductType.FINANCIAL);

        new Expectations(InvestmentService.class){
            {
                invoke(investmentService, "updateProductItem", investData);result=true;
                invoke(investmentService, "accountSnapshot", investData);
                invoke(investmentService, "updateAccount", investData);
                invoke(investmentService, "insertInvestment", investData);result="memberInvestment001";
                invoke(investmentService, "updateProductItem", investData);
                invoke(investmentService, "insertFundRecord", investData, "memberInvestment001");
                invoke(investmentService, "updateAllocateAccount", investData);
                invoke(investmentService, "insertPlatformOperationLog", investData, "memberInvestment001");
                invoke(investmentService, "couponUse", "memberInvestment001", investData);
            }
        };

        /* 执行 */
        invoke(investmentService, "investTransaction", investData);

    }


    @Test
    public void testRemainAmountCompute() {

        /* 初始化 */
        List<MemberReceiptPlan> memberReceiptPlans = new ArrayList<>();
        String interestCalMode = RepaymentType.Month.name();

        MemberReceiptPlan memberReceiptPlan = new MemberReceiptPlan();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        // 设置当前日期在投资起始和结束时间段内
        memberReceiptPlan.setInterestStartTime(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 14);
        memberReceiptPlan.setInterestEndTime(calendar.getTime());
        memberReceiptPlan.setSettlementAmount(new BigDecimal(8000));
        memberReceiptPlan.setPeriod(1);

        memberReceiptPlans.add(memberReceiptPlan);

        MemberReceiptPlan memberReceiptPlan2 = new MemberReceiptPlan();
        calendar.add(Calendar.DAY_OF_MONTH, 21);
        memberReceiptPlan2.setInterestStartTime(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 28);
        memberReceiptPlan2.setInterestEndTime(calendar.getTime());
        memberReceiptPlan2.setPeriod(2);
        memberReceiptPlan2.setSettlementAmount(new BigDecimal(6000));
        memberReceiptPlans.add(memberReceiptPlan2);


        /* 执行 */
        BigDecimal result = invoke(investmentService, "remainAmountCompute", memberReceiptPlans, interestCalMode);

        /* 验证 */
        assertEquals(new BigDecimal(14000),result);


        /**********  以下验证计息方式为一次计息的情况 *********/

        /* 初始化 */
        List<MemberReceiptPlan> memberReceiptPlans2 = new ArrayList<>();
        String interestCalMode2 = RepaymentType.Once.name();
        memberReceiptPlans2.add(memberReceiptPlan);

        /* 执行 */
        BigDecimal result2 = invoke(investmentService, "remainAmountCompute", memberReceiptPlans2, interestCalMode2);

        /* 验证 */
        assertEquals(new BigDecimal(8000),result2);

    }


    @Test
    public void testRemainDaysCompute() {

        /* 初始化 */
        List<MemberReceiptPlan> memberReceiptPlans = new ArrayList<>();
        String interestCalMode = RepaymentType.Month.name();

        MemberReceiptPlan memberReceiptPlan = new MemberReceiptPlan();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        // 设置当前日期在投资起始和结束时间段内
        memberReceiptPlan.setInterestStartTime(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 14);
        memberReceiptPlan.setInterestEndTime(calendar.getTime());
        memberReceiptPlan.setSettlementAmount(new BigDecimal(8000));
        memberReceiptPlan.setPeriod(1);

        memberReceiptPlans.add(memberReceiptPlan);

        MemberReceiptPlan memberReceiptPlan2 = new MemberReceiptPlan();
        calendar.add(Calendar.DAY_OF_MONTH, 21);
        memberReceiptPlan2.setInterestStartTime(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 28);
        memberReceiptPlan2.setInterestEndTime(calendar.getTime());
        memberReceiptPlan2.setPeriod(2);
        memberReceiptPlan2.setSettlementAmount(new BigDecimal(6000));
        memberReceiptPlans.add(memberReceiptPlan2);


        /* 执行 */
        int result = invoke(investmentService, "remainDaysCompute", memberReceiptPlans, interestCalMode);

        /* 验证 */
        assertEquals(result,37);


        /**********  以下验证计息方式为一次计息的情况 *********/

        /* 初始化 */
        List<MemberReceiptPlan> memberReceiptPlans2 = new ArrayList<>();
        String interestCalMode2 = RepaymentType.Once.name();
        memberReceiptPlans2.add(memberReceiptPlan);

        /* 执行 */
        int result2 = invoke(investmentService, "remainDaysCompute", memberReceiptPlans2, interestCalMode2);

        /* 验证 */
        assertEquals(result2,7);

    }

    @Test
    public void testRemainTermsCompute() {

        /* 初始化 */
        List<MemberReceiptPlan> memberReceiptPlans = new ArrayList<>();
        String interestCalMode = RepaymentType.Month.name();

        MemberReceiptPlan memberReceiptPlan = new MemberReceiptPlan();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        // 设置当前日期在投资起始和结束时间段内
        memberReceiptPlan.setInterestStartTime(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 14);
        memberReceiptPlan.setInterestEndTime(calendar.getTime());
        memberReceiptPlan.setSettlementAmount(new BigDecimal(8000));
        memberReceiptPlan.setPeriod(1);

        memberReceiptPlans.add(memberReceiptPlan);

        MemberReceiptPlan memberReceiptPlan2 = new MemberReceiptPlan();
        calendar.add(Calendar.DAY_OF_MONTH, 21);
        memberReceiptPlan2.setInterestStartTime(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 28);
        memberReceiptPlan2.setInterestEndTime(calendar.getTime());
        memberReceiptPlan2.setPeriod(2);
        memberReceiptPlan2.setSettlementAmount(new BigDecimal(6000));
        memberReceiptPlans.add(memberReceiptPlan2);


        /* 执行 */
        int result = invoke(investmentService, "remainTermsCompute", memberReceiptPlans, interestCalMode);

        /* 验证 */
        assertEquals(result,2);


        /**********  以下验证计息方式为一次计息的情况 *********/

        /* 初始化 */
        List<MemberReceiptPlan> memberReceiptPlans2 = new ArrayList<>();
        String interestCalMode2 = RepaymentType.Once.name();
        memberReceiptPlans2.add(memberReceiptPlan);

        /* 执行 */
        int result2 = invoke(investmentService, "remainTermsCompute", memberReceiptPlans2, interestCalMode2);

        /* 验证 */
        assertEquals(result2,7);

    }


    @Test
    public void testComputeFromReceiptPlan() {

        /* 初始化 */
        List<MemberReceiptPlan> memberReceiptPlans = new ArrayList<>();
        String interestCalMode = RepaymentType.Month.name();

        MemberReceiptPlan memberReceiptPlan = new MemberReceiptPlan();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        // 设置当前日期在投资起始和结束时间段内
        memberReceiptPlan.setInterestStartTime(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 14);
        memberReceiptPlan.setInterestEndTime(calendar.getTime());
        memberReceiptPlan.setSettlementAmount(new BigDecimal(8000));
        memberReceiptPlan.setPeriod(1);

        memberReceiptPlans.add(memberReceiptPlan);

        MemberReceiptPlan memberReceiptPlan2 = new MemberReceiptPlan();
        calendar.add(Calendar.DAY_OF_MONTH, 21);
        memberReceiptPlan2.setInterestStartTime(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 28);
        memberReceiptPlan2.setInterestEndTime(calendar.getTime());
        memberReceiptPlan2.setPeriod(2);
        memberReceiptPlan2.setSettlementAmount(new BigDecimal(6000));
        memberReceiptPlans.add(memberReceiptPlan2);


        /* 执行 */
        Map<String,Object> result = invoke(investmentService, "computeFromReceiptPlan", memberReceiptPlans, interestCalMode);

        /* 验证 */
        assertEquals(result.get("remainTerms"),2);
        assertEquals(result.get("remainDays"),37);
        assertEquals(result.get("currentPeriod"),1);
        assertEquals(result.get("remainAmount"),new BigDecimal(14000));


        /**********  以下验证计息方式为一次计息的情况 *********/

        /* 初始化 */
        List<MemberReceiptPlan> memberReceiptPlans2 = new ArrayList<>();
        String interestCalMode2 = RepaymentType.Once.name();
        memberReceiptPlans2.add(memberReceiptPlan);

        /* 执行 */
        Map<String,Object> result2 = invoke(investmentService, "computeFromReceiptPlan", memberReceiptPlans2, interestCalMode2);

        /* 验证 */
        assertEquals(result2.get("remainTerms"),7);
        assertEquals(result2.get("remainDays"),7);
        assertEquals(result2.get("currentPeriod"),1);
        assertEquals(result2.get("remainAmount"),new BigDecimal(8000));

    }


    @Test
    public void testUpdateReceiptPlan() {

        /* 初始化 */
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amount = new BigDecimal(1000);
        final InvestmentService.InvestData investData = investmentService.new InvestData(memberId, productItemId, amount);
        investData.setProductType(ProductType.FINANCIAL);

        MemberInvestment originInvestment = new MemberInvestment();
        originInvestment.setId("originInvestment001");
        investData.setOriginalInvestment(originInvestment);

        ProductItemSetting productItemSetting = new ProductItemSetting();
        productItemSetting.setInterstCalMode(RepaymentType.Month.name());
        investData.setProductItemSetting(productItemSetting);

        final InvestResult investResult = new InvestResult();
        investResult.setInvestmentId("memberInvestment001");

        final List<MemberReceiptPlan> originalReceiptPlans = new ArrayList<>();
        final MemberReceiptPlan memberReceiptPlan = new MemberReceiptPlan();
        memberReceiptPlan.setMemberInvestmentId("memberInvestment001");
        memberReceiptPlan.setPeriod(1);
        final MemberReceiptPlan memberReceiptPlan2 = new MemberReceiptPlan();
        memberReceiptPlan2.setMemberInvestmentId("memberInvestment001");
        memberReceiptPlan2.setPeriod(2);
        final MemberReceiptPlan memberReceiptPlan3 = new MemberReceiptPlan();
        memberReceiptPlan3.setMemberInvestmentId("memberInvestment001");
        memberReceiptPlan3.setPeriod(3);
        originalReceiptPlans.add(memberReceiptPlan);
        originalReceiptPlans.add(memberReceiptPlan2);
        originalReceiptPlans.add(memberReceiptPlan3);

        final Map<String,Object> receiptMap = new HashMap<>();
        receiptMap.put("remainAmount", new BigDecimal(14000));
        receiptMap.put("remainDays", 37);
        receiptMap.put("remainTerms", 2);
        receiptMap.put("currentPeriod", 1);

        new Expectations(InvestmentService.class){
            {
                receiptPlanDao.findByInvestId(investData.getOriginalInvestment().getId());result=originalReceiptPlans;
                invoke(investmentService, "computeFromReceiptPlan", originalReceiptPlans, investData.getProductItemSetting().getInterstCalMode());result=receiptMap;
                /** 注意此处设置times为3,表示预期此mock方法在执行阶段会调用3次,目的是检查for循环是否按照程序逻辑的次数执行的 **/
                receiptPlanDao.update((Map<String, Object>)any);result=1;times=3;
                receiptPlanDao.insert((MemberReceiptPlan)any);
                productItemSettingDao.update((Map<String, Object>)any);result=1;
            }
        };

        /* 执行 */
        invoke(investmentService, "updateReceiptPlan", investData, investResult);

        /* 验证 */
        new Verifications(){
            {
                Map<String, Object> params = null;
                receiptPlanDao.update(params = withCapture());times=3;
                assertEquals(params.get("memberInvestmentId"),memberReceiptPlan.getMemberInvestmentId());
                /** 此处有个遗留问题,上面的update方法在执行过程中走了三次,不知道怎么样能取到每次循环中用到的对应的memberReceiptPlan **/
                //assertEquals(params.get("period"),memberReceiptPlan.getPeriod());
                Map<String, Object> params2 = null;
                productItemSettingDao.update(params2 = withCapture());
                assertEquals(params2.get("term"),receiptMap.get("remainTerms"));
            }
        };

    }

    @Test
    public void testInvestmentReceiptPlan(){

        /* 初始化 */
        final String investmentId = "investment001";

        final MemberInvestment memberInvestment = new MemberInvestment();
        memberInvestment.setId("memberInvestment001");
        memberInvestment.setAmount(new BigDecimal(8000));
        memberInvestment.setProductItemId("productItem001");

        final ProductItem productItem = new ProductItem();
        productItem.setType(ProductType.INVESTMENT);
        productItem.setItemSettingId("itemSetting001");

        final ProductItemSetting productItemSetting = new ProductItemSetting();
        productItemSetting.setInterstCalMode(RepaymentType.Month.name());
        productItemSetting.setInterstStartMode("T1");
        productItemSetting.setTermType(TermType.MONTH.name());

        final List<CalculationOutput> outputs = new ArrayList<>();
        final CalculationOutput calculationOutput = new CalculationOutput();
        outputs.add(calculationOutput);
        calculationOutput.setInterest(new BigDecimal(0.12));
        calculationOutput.setPeroid(5);
        calculationOutput.setPrinciple(new BigDecimal(6000));
        calculationOutput.setPrincilpeAndInterest(new BigDecimal(8000));
        calculationOutput.setRepaymentDate(new Date());

        /** (关键点总结8)如果想mock外部类的静态方法,需要通过这种方式来mock **/
        new MockUp<Calculator>(){
            @Mock
            public List<CalculationOutput> calculate(CalculationInput input) {
                return outputs;
            }
        };


        new Expectations(){
            {
                memberInvestmentDao.getById(investmentId);result=memberInvestment;
                productItemDao.getById(memberInvestment.getProductItemId());result=productItem;
                productItemSettingDao.getById(productItem.getItemSettingId());result=productItemSetting;
                receiptPlanDao.insert((MemberReceiptPlan)any);
            }
        };


        /* 执行 */
        investmentService.investmentReceiptPlan(investmentId);

        /* 验证 */
        new Verifications(){
            {
                MemberReceiptPlan memberReceiptPlan = null;
                receiptPlanDao.insert(memberReceiptPlan = withCapture());
                assertEquals(memberReceiptPlan.getMemberInvestmentId(), memberInvestment.getId());
                assertEquals(memberReceiptPlan.getInterestAmount(), calculationOutput.getInterest());
                assertEquals(memberReceiptPlan.getInterestEndTime(), calculationOutput.getRepaymentDate());
                assertEquals(memberReceiptPlan.getActualInterestAmount(),new BigDecimal(0));
                assertEquals(memberReceiptPlan.getActualSettlementAmount(),new BigDecimal(0));
                assertEquals(memberReceiptPlan.getPeriod(),calculationOutput.getPeroid());
                assertEquals(memberReceiptPlan.getStatus(),ReceiptPlanStatus.INVESTING.name());
                assertEquals(memberReceiptPlan.getInterestStartTime(), DateUtils.addMonths(calculationOutput.getRepaymentDate(), -1));
                assertEquals(memberReceiptPlan.getCouponProfit(),false);
            }
        };


    }

    @Test
    public void testInvestmentFullReceiptPlans(){
        final String productItemId = "productItem001";

        final ProductItem productItem = new ProductItem();
        productItem.setType(ProductType.INVESTMENT);
        productItem.setStatus(ProductStatus.FULL);
        productItem.setItemSettingId("itemSetting001");
        productItem.setId(productItemId);

        final ProductItemSetting productItemSetting = new ProductItemSetting();
        productItemSetting.setInterstCalMode(RepaymentType.Month.name());
        productItemSetting.setInterstStartMode("T1");
        productItemSetting.setTermType(TermType.MONTH.name());

        final List<MemberInvestment> investmentList = new ArrayList<>();
        final MemberInvestment memberInvestment = new MemberInvestment();
        memberInvestment.setId("memberInvestment001");
        memberInvestment.setStatus(InvestmentStatus.INVESTING.name());
        memberInvestment.setAmount(new BigDecimal(6000));
        investmentList.add(memberInvestment);

        final List<CalculationOutput> outputs = new ArrayList<>();
        final CalculationOutput calculationOutput = new CalculationOutput();
        outputs.add(calculationOutput);
        calculationOutput.setInterest(new BigDecimal(0.12));
        calculationOutput.setPeroid(5);
        calculationOutput.setPrinciple(new BigDecimal(6000));
        calculationOutput.setPrincilpeAndInterest(new BigDecimal(8000));
        calculationOutput.setRepaymentDate(new Date());

        final MemberAccount primeAccount = new MemberAccount();
        primeAccount.setId("primeAccount001");

        new MockUp<Calculator>(){
            @Mock
            public List<CalculationOutput> calculate(CalculationInput input) {
                return outputs;
            }
        };

        new Expectations(InvestmentService.class){
            {
                productItemDao.getById(productItemId);result=productItem;
                productItemSettingDao.getById(productItem.getItemSettingId());result=productItemSetting;
                memberInvestmentDao.getByItemId(productItem.getId());result=investmentList;

                investmentService.couponUpdateStatus((MemberInvestment)any,MemberCouponStatus.FROZEN,
                        MemberCouponStatus.USED);

                memberAccountDao.getByMemberIdAndType(anyString,
                        AccountType.PRIMARY.name());result=primeAccount;

                investmentService.createAccountSnapshot(primeAccount);result="accountSnapshot001";
                memberAccountDao.updateUnfrozenAccountOnFull(primeAccount.getId(), memberInvestment.getAmount());result=1;
                investmentService.couponReceiptPlan(memberInvestment,productItemSetting,(MemberReceiptPlan)any);

                platformAccountDao.updateInvest(memberInvestment.getAmount());result=1;

                borrowerBorrowRecordDao.update((Map<String, Object>)any);result=1;
            }
        };

        investmentService.investmentFullReceiptPlans(productItemId);

        new Verifications(){
            {

            }
        };

    }


    @Test
    public void testXpressDedemptionValidation(){
        /** 赎回金额校验,赎回金额1000,随鑫宝账户总额8000,白名单设置赎回限额为5000,当日累计赎回过的金额为1000+2000 **/
        final XpressDedemptionIn xpressDedemptionIn = new XpressDedemptionIn();
        xpressDedemptionIn.setMemberId("member001");
        xpressDedemptionIn.setAmount(new BigDecimal(1000));
        xpressDedemptionIn.setTradePassword("admin");
        final Member member = new Member();
        member.setTradePassword(Digest.shaDigest(xpressDedemptionIn.getTradePassword()));
        final MemberAccount xpressMemberAccount = new MemberAccount();
        xpressMemberAccount.setTotalAmount(new BigDecimal(8000));

        final XpressWhitelist xpressWhitelist = new XpressWhitelist();
        // 设置赎回限额
        xpressWhitelist.setDailyRedeemLimit(new BigDecimal(5000));

        final List<MemberFundRecord> memberFundRecords = new ArrayList<>();
        MemberFundRecord memberFundRecord1 = new MemberFundRecord();
        memberFundRecord1.setFundAmount(new BigDecimal(1000));
        memberFundRecords.add(memberFundRecord1);
        MemberFundRecord memberFundRecord2 = new MemberFundRecord();
        memberFundRecord2.setFundAmount(new BigDecimal(2000));
        memberFundRecords.add(memberFundRecord2);
        final List<MemberFundRecord> memberFundRecordsSuccess = new ArrayList<>();

        new Expectations(){
            {
                memberDao.getById(xpressDedemptionIn.getMemberId());result=member;
                MemberAccountCondition xpressAccountCondition = withCapture(new ArrayList<MemberAccountCondition>());
                memberAccountDao.getMemberAccountByMemberIdAndType(xpressAccountCondition);result=xpressMemberAccount;
                xpressWhitelistDao.getXpressWhitelistByMemberId(xpressDedemptionIn.getMemberId());result=xpressWhitelist;
                MemberFundRecordCondition memberFundRecordCondition = withCapture(new ArrayList<MemberFundRecordCondition>());
                memberFundRecordDao.getMemberFundRecordsByAccountIdAndType(memberFundRecordCondition);result=memberFundRecords;
                MemberFundRecordCondition memberFundRecordConditionSuccess = withCapture(new ArrayList<MemberFundRecordCondition>());
                memberFundRecordDao.getMemberFundRecordsByAccountIdAndType(memberFundRecordConditionSuccess);result=memberFundRecordsSuccess;
            }
        };

         /* 执行 */
        investmentService.xpressDedemptionValidation(xpressDedemptionIn);

    }

//    @Test
//    public void testGetMemberInvestmentsReceiptsPlan(){
//        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add(OctopusProperty.memberId.name(),"member001");
//        params.add(OctopusProperty.investmentId.name(), "investment001");
//
//        final MemberInvestment investment = new MemberInvestment();
//        investment.setMemberId(params.getFirst(OctopusProperty.memberId.name()));
//
//        final List<ReceiptsPlan> receiptsPlans = new ArrayList<>();
//        ReceiptsPlan receiptsPlan = new ReceiptsPlan();
//        receiptsPlans.add(receiptsPlan);
//
//        new Expectations(){
//            {
//                memberInvestmentDao.getById(params.getFirst(OctopusProperty.investmentId.name()));result=investment;
//                receiptPlanDao.getMemberInvestmentsReceiptsPlan(params.getFirst(OctopusProperty.investmentId.name()));result=receiptsPlans;
//            }
//        };
//
//        Pagination<ReceiptsPlan> result = investmentService.getMemberInvestmentsReceiptsPlan(params);
//
//        assertEquals(result.size(), 1);
//
//    }

//    @Test
//    public void testGetMemberReceiptDate(){
//        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add(OctopusProperty.startDate.name(),"2015-10-10");
//        params.add(OctopusProperty.endDate.name(), "2015-11-11");
//
//        final List<String> list = new ArrayList<>();
//        list.add("111");
//
//        new Expectations(){
//            {
//                receiptPlanDao.getMemberReceiptDate(params.toSingleValueMap());result=list;
//            }
//        };
//
//        List<String> result = investmentService.getMemberReceiptDate(params);
//
//        assertEquals(result.size(), 1);
//
//    }

//    @Test
//    public void testGetMemberReceiptsWithTime(){
//
//        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//
//        final List<ReceiptsPlan> receiptsPlans = new ArrayList<>();
//        ReceiptsPlan receiptsPlan = new ReceiptsPlan();
//        receiptsPlans.add(receiptsPlan);
//
//        new Expectations(){
//            {
//                receiptPlanDao.getMemberReceiptsWithTime(params.toSingleValueMap());result=receiptsPlans;
//            }
//        };
//
//        List<ReceiptsPlan> result = investmentService.getMemberReceiptsWithTime(params);
//
//        assertEquals(result.size(),1);
//
//    }

    @Test
    public void testGetMemberInvestTotal(){
        final String memberId = "member001";
        final String productType = "INVESTMENT";

        final BigDecimal amount = new BigDecimal(8000);
        final BigDecimal collectInterest = new BigDecimal(200);
        final BigDecimal collectPrincipal = new BigDecimal(6000);
        final BigDecimal sumProfit = new BigDecimal(600);
        final BigDecimal inSumAmount = new BigDecimal(600);
        final int inSumCount = 6;
        final BigDecimal outSumAmount = new BigDecimal(3000);
        final int outSumCount = 8;

        new Expectations(){
            {
                memberInvestmentDao.getMemberInvestAmount(memberId, productType);result=amount;
                memberInvestmentDao.getMemberInvestCollectInterest(memberId, productType);result=collectInterest;
                memberInvestmentDao.getMemberInvestCollectPrincipal(memberId, productType);result=collectPrincipal;
                memberInvestmentDao.getMemberInvestSumProfit(memberId, productType);result=sumProfit;
                memberInvestmentDao.getMemberTransferInSum(memberId);result=inSumAmount;
                memberInvestmentDao.getMemberTransferInCount(memberId);result=inSumCount;
                memberInvestmentDao.getMemberTransferOutSum(memberId);result=outSumAmount;
                memberInvestmentDao.getMemberTransferOutCount(memberId);result=outSumCount;
            }
        };

        InvestmentTotalVo investmentTotalVo = investmentService.getMemberInvestTotal(memberId,productType);

        assertEquals(investmentTotalVo.getAmount(),amount);
//        assertEquals(investmentTotalVo.getCollectInterest(),collectInterest);
//        assertEquals(investmentTotalVo.getCollectPrincipal(),collectPrincipal);
//        assertEquals(investmentTotalVo.getSumProfit(),sumProfit);
//        assertEquals(investmentTotalVo.getInSumAmount(),inSumAmount);
//        assertEquals(investmentTotalVo.getInSumCount(),inSumCount);
//        assertEquals(investmentTotalVo.getOutSumAmount(),outSumAmount);
//        assertEquals(investmentTotalVo.getOutSumCount(),outSumCount);
    }

    @Test
    public void testGetMemberInvestmentRecord(){

        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(OctopusProperty.status.name(),"1");
        params.add(OctopusProperty.productType.name(), "1");
        params.add(OctopusProperty.page.name(), "1");
        params.add(OctopusProperty.pageSize.name(), "10");
        params.add(OctopusProperty.memberId.name(),"member001");


        final Map<String, String> query = new HashMap<>();
        query.put(OctopusProperty.page.name(), params.getFirst(OctopusProperty.page.name()));
        query.put(OctopusProperty.pageSize.name(), params.getFirst(OctopusProperty.pageSize.name()));
        query.put(OctopusProperty.memberId.name(), params.getFirst(OctopusProperty.memberId.name()));
        query.put(OctopusProperty.status.name(), InvestmentStatus.valueOfName(Integer.parseInt("1")));
        query.put(OctopusProperty.productType.name(), Type.valueOf(Integer.parseInt("1")).getCode());

        final List<InvestmentRecordVo> list = new ArrayList<>();
        list.add(new InvestmentRecordVo());

        new Expectations(){
            {
                // Map<String, String> query = withCapture(new ArrayList<Map<String, String>>());
                memberInvestmentDao.getMemberInvestmentRecordCount(query);result=2;
                memberInvestmentDao.getMemberInvestmentRecord(query);result=list;
            }
        };

        //PageVo<InvestmentRecordVo> result = investmentService.getMemberInvestmentRecord(params);

        //assertEquals(result.getData().size(),1);

    }

    @Test
    public void testCouponValidate(){

        /* 初始化 */
        final String memberId = "member001";
        final String productItemId = "productItem001";
        final BigDecimal amount = new BigDecimal(6000);
        final InvestmentService.InvestData investData = investmentService.new InvestData(memberId, productItemId, amount);
        investData.setProductType(ProductType.FINANCIAL);
        investData.setMemberCouponId(1001L);

        ProductItemSetting itemSetting = new ProductItemSetting();
        investData.setProductItemSetting(itemSetting);
        // 标的期限在加息券最小和最大之间
        itemSetting.setTerm(5);
        //设置类型为月标
        itemSetting.setTermType(TermType.MONTH.name());
        // 设置计息方式为月
        itemSetting.setInterstCalMode(RepaymentType.Month.name());
        itemSetting.setInterstStartMode("T1");

        investData.setIsNovice("Y");

        // mock
        final MemberCoupon memberCoupon = new MemberCoupon();
        memberCoupon.setMemberUuid(memberId);
        memberCoupon.setCouponId(8001L);
        memberCoupon.setCouponRuleId(900L);
        // 设置状态为未使用
        memberCoupon.setStatus(MemberCouponStatus.UNUSED);
        // 设置过期时间大于今天
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        memberCoupon.setExpiredTime(calendar.getTime());
        final Coupon coupon = new Coupon();
        final CouponRule couponRule = new CouponRule();
        couponRule.setSupportNovice(true);
        couponRule.setMinMonth(1);
        couponRule.setMaxMonth(12);
        couponRule.setAnnualYield(new BigDecimal(0.12));
        // 加息券规则中起投金额
        couponRule.setMinInvestmentAmount(new BigDecimal(1000));


        new Expectations(){
            {
                memberCouponDao.getMemberCouponById(investData.getMemberCouponId());result=memberCoupon;
                couponDao.getCouponById(memberCoupon.getCouponId());result=coupon;
                couponRuleDao.getCouponRuleById(memberCoupon.getCouponRuleId());result=couponRule;

            }
        };

        investmentService.couponValidate(investData);

    }

    @Test
    public void testCalInterestDate(){
        final ProductItemSetting productItemSetting1 = new ProductItemSetting();
        productItemSetting1.setInterstStartMode("T0");
        Date result1 = invoke(investmentService,"calcInterestDate",productItemSetting1);
        //assertEquals(result1.getDay(), DateUtils.addDays(new Date(), 0).getDay());

        final ProductItemSetting productItemSetting2 = new ProductItemSetting();
        productItemSetting2.setInterstStartMode("T1");
        Date result2 = invoke(investmentService,"calcInterestDate",productItemSetting2);
        //assertEquals(result2.getDay(), DateUtils.addDays(new Date(), 1).getDay());

    }

    //@Test
    public void testInvestmentRevoked(){
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        final String memberId = "member001";
        final String investmentId = "investment001";
        params.add(OctopusProperty.memberId.name(),memberId);
        params.add(OctopusProperty.investmentId.name(), investmentId);

        final Member member = new Member();
        member.setMemberType(MemberType.INDIVIDUAL.name());

        final MemberInvestment investment = new MemberInvestment();
        investment.setMemberId(memberId);
        investment.setProductItemId("productItem001");
        investment.setAmount(new BigDecimal(8000));
        investment.setStatus(InvestmentStatus.INVESTING.name());

        final ProductItem productItem = new ProductItem();
        productItem.setId(investment.getProductItemId());
        productItem.setType(ProductType.INVESTMENT);
        productItem.setStatus(ProductStatus.APPROVED);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        productItem.setReleaseTime(calendar.getTime());

        final ProductItemInvestInfo productItemInvestInfo = new ProductItemInvestInfo();
        calendar.add(Calendar.DAY_OF_MONTH, 14);
        //设置流标时间为今天以后
        productItemInvestInfo.setExpiredTime(calendar.getTime());

        final MemberAccount memberAccount = new MemberAccount();
        memberAccount.setId("memberAccount001");
        memberAccount.setFrozenAmount(new BigDecimal(1000));
        memberAccount.setAmountBalance(new BigDecimal(1000));
        memberAccount.setAmountBalanceChecksum("xxx");
        memberAccount.setIncommingAmount(new BigDecimal(1000));
        memberAccount.setRechargeAmount(new BigDecimal(1000));
        memberAccount.setTotalAmount(new BigDecimal(1000));
        memberAccount.setWithdrawAmount(new BigDecimal(1000));


        new Expectations(){
            {
                memberDao.getById(memberId);result=member;
                memberInvestmentDao.getById(investmentId);result=investment;
                productItemDao.getById(investment.getProductItemId());result=productItem;
                productItemInvestInfoDao.getByItemId(investment.getProductItemId());result=productItemInvestInfo;
                Map<String, Object> investParam = new HashMap<>();
                investParam.put(OctopusProperty.id.name(), investmentId);
                investParam.put(OctopusProperty.status.name(), InvestmentStatus.REVOKED.name());
                // 参数必须完全匹配,才能够走此mock,得到预期结果
                memberInvestmentDao.update(investParam);result=1;

                MemberAccountCondition memberAccountCondition = new MemberAccountCondition();
                memberAccountCondition.setMemberId(memberId);
                memberAccountCondition.setType(AccountType.PRIMARY.name());
                memberAccountDao.getMemberAccountByMemberIdAndType(memberAccountCondition);result=memberAccount;

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

                MemberAccountCondition updateAccountCondition = new MemberAccountCondition();
                updateAccountCondition.setId(memberAccount.getId());
                updateAccountCondition.setAmount(investment.getAmount());
                updateAccountCondition.setAmountBalanceFlag(AccountAmountType.INCREASE.name());
                updateAccountCondition.setFrozenAmountFlag(AccountAmountType.REDUCE.name());
                memberAccountDao.updateMemberAccountByCondition(updateAccountCondition);result=1;


            }
        };

        RevokedVo revokedVo = investmentService.investmentRevoked(params);
    }


    @Test
    public void testCouponUpdateStatus(){
        final MemberInvestment memberInvestment = new MemberInvestment();
        memberInvestment.setMemberCouponId(1001L);
        final MemberCouponStatus preStatus = MemberCouponStatus.UNUSED;
        final MemberCouponStatus status = MemberCouponStatus.USED;

        final MemberCoupon memberCoupon = new MemberCoupon();
        memberCoupon.setStatus(preStatus);

        new Expectations(){
            {
                memberCouponDao.getMemberCouponById(memberInvestment.getMemberCouponId());result=memberCoupon;
            }
        };

        investmentService.couponUpdateStatus(memberInvestment,preStatus,status);

        new Verifications(){
            {
                MemberCoupon tmp = null;
                memberCouponDao.updateMemberCoupon(tmp=withCapture());
                assertEquals(tmp.getStatus(),status);
            }
        };

    }

    @Test
    public void testCouponReceiptPlan(){

        final MemberInvestment memberInvestment = new MemberInvestment();
        memberInvestment.setAmount(new BigDecimal(800));
        memberInvestment.setMemberCouponId(1001L);

        final ProductItemSetting productItemSetting = new ProductItemSetting();
        productItemSetting.setInterstCalMode(RepaymentType.Month.name());
        productItemSetting.setTerm(12);
        productItemSetting.setTermType(TermType.MONTH.name());
        productItemSetting.setInterstStartMode(InterestStartMode.T1.name());

        final MemberReceiptPlan memberReceiptPlan = new MemberReceiptPlan();
        memberReceiptPlan.setPeriod(12);

        final MemberCoupon memberCoupon = new MemberCoupon();
        final CouponRule couponRule = new CouponRule();
        couponRule.setMaxInvestmentAmount(new BigDecimal(8000));
        couponRule.setAnnualYield(new BigDecimal(0.12));

        new Expectations(){
            {
                memberCouponDao.getMemberCouponById(memberInvestment.getMemberCouponId());result=memberCoupon;
                couponRuleDao.getCouponRuleById(memberCoupon.getCouponRuleId());result=couponRule;

            }
        };

        investmentService.couponReceiptPlan(memberInvestment,productItemSetting,memberReceiptPlan);

        new Verifications(){
            {
                MemberReceiptPlan temp = null;
                receiptPlanDao.insert(temp = withCapture());
                assertEquals(temp.getPeriod(),13);
            }
        };


    }






}
xport PATH=$PATH:$GRADLE_HOME/bin
