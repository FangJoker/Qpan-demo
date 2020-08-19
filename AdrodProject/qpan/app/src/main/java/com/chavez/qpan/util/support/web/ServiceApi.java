package com.chavez.qpan.util.support.web;

public class ServiceApi {
    private final static String HOST = "http://127.0.0.1";

    public static class User {
        /**
         * method: get
         * param : "phoneNumber":"String"
         */
        public final static String REGISTER_GET_VERIFY_CODE = HOST + "/api/q-pan/user/getVerifyCode";
        /**
         * param:
         * {
         * "account": "string",
         * "verificationCode": "string"
         * }
         * method: post
         */
        public final static String REGISTER_ACTION = HOST + "/api/q-pan/user/register";

        /**
         * {
         * "account": "string",
         * "password": "string"
         * }
         * method: post
         */
        public final static String LOGIN_ACTION = HOST + "/api/q-pan/user/login";

        /**
         * method: get
         */
        public final static String GET_INDIVIDUAL_DIR = HOST + "/api/q-pan/user/dir";
        /**
         * method: get
         * param : "target":"String"
         */
        public final static String SEARCH_FILE = HOST + "/api/q-pan/user/searchfile";

        /**
         * method: get
         * param: "key":String
         */
        public final static String SEARCH_SHARE_FILE = HOST + "/api/q-pan/user/searchShareFile";

        /**
         * method: post
         * param:
         * {
         *   "passWord": "string",
         *   "uuids": [
         *     "string"
         *   ]
         * }
         */
        public final static String SHARE_FILE = HOST + "/api/q-pan/user/shareFile";
    }

    public static class File {
        /**
         * method: get
         * param : "token":"String"
         * param : "uuid":"String"
         */
        public final static String DOWNLOAD_FILE = HOST + "/api/q-pan/file/download";

        /**
         * method: get
         * param : "uuid":"String"
         * param : "password":"String"
         */
        public final static String DOWNLOAD_SHARED_FILE = HOST + "/api/q-pan/file/download/share";

        /**
         * method: post
         * param : "token":"String"
         * param : "pathUuid":"String"
         * param : "chunk":"int"
         * param : "chunks":"int"
         *  param : "file":"File"
         */
        public final static String UPLOAD_FILE = HOST + "/api/q-pan/file/upload";

        /**
         * method: delete
         * param : "uuid":"String"
         */
        public final static String DELETE_FILE = HOST + "/api/q-pan/file/delete";

        public final static String NEW_FLODER = HOST + "/api/q-pan/file/mkdir";

    }


}
