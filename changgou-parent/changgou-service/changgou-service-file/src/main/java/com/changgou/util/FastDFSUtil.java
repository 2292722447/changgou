package com.changgou.util;

import com.changgou.file.FastDFSFile;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 实现文件管理
 *            文件上传
 *            文件下载
 *            文件删除
 *            文件信息获取
 *            Storage信息获取
 *            Tracker信息获取
 */
public class FastDFSUtil {


    /**
     * 加载Tracker链接信息
     */
    static {
        //查找cclasspath下的文件路径
        String filename = new ClassPathResource("fdfs_client.conf").getPath();
        // 加载Tracker链接信息
        try {
            try {
                ClientGlobal.init(filename);
            } catch (MyException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 文件上传
     */
    public static String[] upload(FastDFSFile fastDFSFile) throws Exception {

        //附加参数
        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("拍摄地址", "北京");
//        //创建一个Tracker访问的客户端对象TrackerClient
//        TrackerClient trackerClient = new TrackerClient();
//        //通过TrackerClient访问TrackerServer服务，获取信息链接
//        TrackerServer trackerServer = trackerClient.getConnection();
//        //通过TrackerServer的链接信息可以获取Storage的链接信息，创建StorageClient对象存储Storage的链接信息
//        StorageClient storageClient = new StorageClient(trackerServer, null);

        StorageClient storageClient = getStorageClient();

        /**
         *  //通过StorageClient访问Storage 实现文件上传 并且获取文件上传后的存储信息
         *  1.文件上传的字节数组
         *  2。文件的扩展名
         *  3.附加参数
         */

        /**
         * upload[0]:上传文件所存储的storage 的组名字  group1
         * upload[1] 文件存储到storage上的文件名字
         */
        String[] uploads = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meta_list);


        return uploads;

    }


    /***
     * 获取文件信息
     * @param groupName:组名
     * @param remoteFileName：文件存储完整名
     */

    public static FileInfo getFile(String groupName, String remoteFileName) throws Exception {
//        //创建一个Tracker访问的客户端对象TrackerClient
//        TrackerClient trackerClient = new TrackerClient();
//        //通过TrackerClient访问TrackerServer服务，获取信息链接
//        TrackerServer trackerServer = trackerClient.getConnection();
//        // 通过TrackerServer的链接信息可以获取Storage的链接信息，创建StorageClient对象存储Storage的链接信息
//        StorageClient storageClient = new StorageClient(trackerServer, null);

        StorageClient storageClient = getStorageClient();
        //获取文件信息
        FileInfo file_info = storageClient.get_file_info(groupName, remoteFileName);
        return file_info;
    }

    /***
     * 文件下载
     * @param groupName:组名
     * @param remoteFileName：文件存储完整名
     * @return
     */

    public static InputStream downFile(String groupName, String remoteFileName) throws Exception {
//        //创建一个Tracker访问的客户端对象TrackerClient
//        TrackerClient trackerClient = new TrackerClient();
//        //通过TrackerClient访问TrackerServer服务，获取信息链接
//        TrackerServer trackerServer = trackerClient.getConnection();
//        // 通过TrackerServer的链接信息可以获取Storage的链接信息，创建StorageClient对象存储Storage的链接信息
//        StorageClient storageClient = new StorageClient(trackerServer, null);


        StorageClient storageClient = getStorageClient();
        //文件下载
        byte[] buffer = storageClient.download_file(groupName, remoteFileName);
        return new ByteArrayInputStream(buffer);

    }

    /***
     * 文件删除实现
     * @param groupName:组名
     * @param remoteFileName：文件存储完整名
     */
    public static void deleteFile(String groupName, String remoteFileName) throws Exception {
//        //创建一个Tracker访问的客户端对象TrackerClient
//        TrackerClient trackerClient = new TrackerClient();
//        //通过TrackerClient访问TrackerServer服务，获取信息链接
//        TrackerServer trackerServer = trackerClient.getConnection();
//        // 通过TrackerServer的链接信息可以获取Storage的链接信息，创建StorageClient对象存储Storage的链接信息
//        StorageClient storageClient = new StorageClient(trackerServer, null);

        StorageClient storageClient = getStorageClient();
        int i = storageClient.delete_file(groupName, remoteFileName);

    }

    /***
     * 获取组信息
     * @param
     */
    public static StorageServer getStorages() throws Exception {

        //创建一个Tracker访问的客户端对象TrackerClient
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient访问TrackerServer服务，获取信息链接
        TrackerServer trackerServer = trackerClient.getConnection();
        //获取信息
        StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);

        return storeStorage;
    }

    /***
     * 根据文件组名和文件存储路径获取Storage服务的IP、端口信息
     *
     */
    public static ServerInfo[] getServierInfo(String groupName, String remoteFileName) throws Exception {
        //创建一个Tracker访问的客户端对象TrackerClient
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient访问TrackerServer服务，获取信息链接
        TrackerServer trackerServer = trackerClient.getConnection();


        return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
    }

    /***
     * 获取Tracker服务地址
     */
    public static String getTrackerInfo() throws Exception {
        //创建一个Tracker访问的客户端对象TrackerClient
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient访问TrackerServer服务，获取信息链接
        TrackerServer trackerServer = trackerClient.getConnection();

        //获取id和Http端口
        int tracker_http_port = ClientGlobal.getG_tracker_http_port();
        String ip = trackerServer.getInetSocketAddress().getHostString();
        String url = "http://" + ip + ":" + tracker_http_port;

        return url;
    }


    public static void main(String[] args) throws Exception {

//        FileInfo fileInfo = getFile("gorup1", "M00/00/00/wKgAb161luGAQvh4ACzbRj3mRxA949.png");
//        System.out.println(fileInfo.getSourceIpAddr());
//        System.out.println(fileInfo.getFileSize());


//        InputStream is = downFile("gorup1", "M00/00/00/wKgAb161luGAQvh4ACzbRj3mRxA949.png");
////        //将文件写入本地磁盘
////        FileOutputStream  os= new FileOutputStream("D:/1.png");
////        //定义一个缓冲区
////        byte[] buffer = new byte[1024];
////        while (is.read(buffer)!=-1){
////            os.write(buffer);
////        }
////        os.flush();
////        os.close();
////        is.close();


        // deleteFile("gorup1", "M00/00/00/wKgAb161luGAQvh4ACzbRj3mRxA949.png");


        //获取
//        StorageServer storages = getStorages();
//        System.out.println(storages.getStorePathIndex());
//        System.out.println(storages.getInetSocketAddress());


//        //获取storage组的ip
//
//        ServerInfo[] info = getServierInfo("group1", "M00/00/00/wKgAb161luGAQvh4ACzbRj3mRxA949.png");
//
//        for (ServerInfo group : info){
//            System.out.println(group.getIpAddr());
//            System.out.println(group.getPort());
//        }
//    }


        System.out.println(getTrackerInfo());
    }


    /***
     * 获取TrackerServer
     */
    public static TrackerServer getTrackerServer() throws Exception {

         TrackerClient trackerClient  =  new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerServer;


    }

    /***
     * 获取StorageClient
     * @return
     * @throws Exception
     */
    public static StorageClient getStorageClient() throws Exception{

         TrackerServer trackerServer =getTrackerServer();
         StorageClient storageClient = new StorageClient(trackerServer,null);

         return  storageClient;
    }

}