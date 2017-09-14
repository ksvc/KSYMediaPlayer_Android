package com.ksyun.player.now.bean;

import java.io.Serializable;
import java.util.List;

public class LiveBean{

    /**
     * Data : {"RetCode":0,"RetMsg":"success","Detail":[{"PlayURL":["http://www.ksyun.com"],"CoverURL":"https://www.google.com","VideoID":1},{"PlayURL":["http://blog.yangfan21.cn"],"CoverURL":"https://www.google.com","VideoID":4}]}
     * RequestId :
     */

    private DataBean Data;
    private String RequestId;
    public LiveBean(){}
    public DataBean getData() {
        return Data;
    }

    public void setData(DataBean Data) {
        this.Data = Data;
    }

    public String getRequestId() {
        return RequestId;
    }

    public void setRequestId(String RequestId) {
        this.RequestId = RequestId;
    }

    public static class DataBean {

        private int RetCode;
        private String RetMsg;
        private List<DetailBean> Detail;
        public DataBean(){}
        public int getRetCode() {
            return RetCode;
        }

        public void setRetCode(int RetCode) {
            this.RetCode = RetCode;
        }

        public String getRetMsg() {
            return RetMsg;
        }

        public void setRetMsg(String RetMsg) {
            this.RetMsg = RetMsg;
        }

        public List<DetailBean> getDetail() {
            return Detail;
        }

        public void setDetail(List<DetailBean> Detail) {
            this.Detail = Detail;
        }

        public static class DetailBean  implements Serializable{

            private List<String> CoverURL;
            private int VideoID;
            private List<String> PlayURL;
            private String VideoTitle;
            public DetailBean(){}
            public List<String> getCoverURL() {
                return CoverURL;
            }

            public void setCoverURL(List<String> CoverURL) {
                this.CoverURL = CoverURL;
            }

            public int getVideoID() {
                return VideoID;
            }

            public void setVideoID(int VideoID) {
                this.VideoID = VideoID;
            }

            public List<String> getPlayURL() {
                return PlayURL;
            }

            public void setPlayURL(List<String> PlayURL) {
                this.PlayURL = PlayURL;
            }
            public String getVideoTitle() {
                return VideoTitle;
            }

            public void setVideoTitle(String videoTitle) {
                VideoTitle = videoTitle;
            }
        }
    }
}
