package vn.vplay.sdk.t000a;

import java.util.List;

public interface KcattaListener {
    void onPayProductSuccess(String productId);
    void onPayProductError(Error error);
    void onGetProductsSuccess(List<ProductInfo> products);
    void onGetProductError(Error error);
}
