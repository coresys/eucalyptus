import com.eucalyptus.util.EntityWrapper;
import com.eucalyptus.util.StorageProperties;
import com.eucalyptus.util.WalrusProperties;
import com.eucalyptus.util.EntityWrapper;
import com.eucalyptus.bootstrap.Component;
import groovy.sql.Sql;
import edu.ucsb.eucalyptus.cloud.entities.BucketInfo;
import edu.ucsb.eucalyptus.cloud.entities.AOEVolumeInfo;
import edu.ucsb.eucalyptus.cloud.entities.AOEMetaInfo;
import com.eucalyptus.util.DatabaseUtil;


/* euca.*.dir are set by euca_upgrade when calling this script */
baseDir = "${System.getProperty('euca.old')}/var/lib/eucalyptus/db";
targetDir = "${System.getProperty('euca.new')}/var/lib/eucalyptus/db";
targetDbPrefix= "eucalyptus"
Component c = Component.db
c.setUri( "jdbc:hsqldb:file:${targetDir}/${targetDbPrefix}" );
System.setProperty("euca.db.host", "jdbc:hsqldb:file:${targetDir}/${targetDbPrefix}")
System.setProperty("euca.db.password", "${System.getenv('EUCALYPTUS_DB')}");
db_pass = System.getenv('EUCALYPTUS_DB');
System.setProperty("euca.log.level", 'INFO');

def getSqlStorage() {
	source = new org.hsqldb.jdbc.jdbcDataSource();
	source.database = "jdbc:hsqldb:file:${baseDir}/eucalyptus_storage";
	source.user = 'sa';
	source.password = db_pass;
	return new Sql(source);
}

def getSqlWalrus() {
	source = new org.hsqldb.jdbc.jdbcDataSource();
	source.database = "jdbc:hsqldb:file:${baseDir}/eucalyptus_walrus";
	source.user = 'sa';
	source.password = db_pass;
	return new Sql(source);
}

oldDbStorage = getSqlStorage();
oldDbWalrus = getSqlWalrus();

try {
  if( !targetDir.equals(baseDir) ) {
    new File("${baseDir}").eachFileMatch(~/.*\.(script)|.*\.(log)/) { baseDb ->
      println "\nPreparing ${baseDb.absolutePath}..."
      targetDb = new File(baseDb.absolutePath.replaceAll(baseDir,targetDir));
      if(targetDb.exists()) {
        println "Bailing out of upgrade: ${targetDb.absolutePath} already exists!"
        println "It looks like a previous upgrade may have failed!"
        println "You will need to manually cleanup ${targetDir} before proceeding." 
        System.exit(1)
      } else {
        println "Copying ${baseDb.absolutePath} to ${targetDb.absolutePath}..."
        targetDb.write(baseDb.text);
      }
    }
  }
  new after_database(new Binding([db_pass:db_pass])).run();
} catch( Throwable t ) {
  t.printStackTrace();
  t?.getCause().printStackTrace();
  System.exit(1);
}
System.out.println(Thread.currentThread().getStackTrace());

def updateBuckets() {
	EntityWrapper<BucketInfo> dbBucket = new EntityWrapper<BucketInfo>( WalrusProperties.DB_NAME );
	BucketInfo bucketInfo = new BucketInfo();
	List<BucketInfo> buckets = dbBucket.query(bucketInfo);
	for(BucketInfo bucket : buckets) {
		bucket.setLoggingEnabled(false);		
	}
	dbBucket.commit();
}

System.out.println(Thread.currentThread().getStackTrace());
updateBuckets();

System.out.println(Thread.currentThread().getStackTrace());
oldDbStorage.rows('SELECT * FROM LVMMETADATA').each {
	EntityWrapper<AOEMetaInfo> dbStorage = new EntityWrapper<AOEMetaInfo>( StorageProperties.DB_NAME );
	try {
		AOEMetaInfo metaInfo = new AOEMetaInfo(it.HOSTNAME);
		metaInfo.setMajorNumber(it.MAJOR_NUMBER);
		metaInfo.setMinorNumber(it.MINOR_NUMBER);
		dbStorage.add(metaInfo);
		dbStorage.commit();
	} catch(Throwable t) {
		t.printStackTrace();
		dbStorage.rollback();
	}	
}

System.out.println(Thread.currentThread().getStackTrace());
oldDbStorage.rows('SELECT * FROM LVMVOLUMES').each {
	EntityWrapper<AOEVolumeInfo> dbStorage = new EntityWrapper<AOEVolumeInfo>( StorageProperties.DB_NAME );
	try {
		AOEVolumeInfo volumeInfo = new AOEVolumeInfo(it.VOLUME_NAME);
		volumeInfo.setMajorNumber(it.MAJOR_NUMBER);
		volumeInfo.setMinorNumber(it.MINOR_NUMBER);
		volumeInfo.setLoDevName(it.LODEV_NAME);
		volumeInfo.setLoFileName(it.LOFILE_NAME);
		volumeInfo.setVbladePid(it.VBLADE_PID);
		volumeInfo.setScName(it.SC_NAME);
		volumeInfo.setPvName(it.PV_NAME);
		volumeInfo.setLvName(it.LV_NAME);
		volumeInfo.setVgName(it.VG_NAME);
		volumeInfo.setSize(it.SIZE);
		volumeInfo.setStatus(it.STATUS);
		volumeInfo.setSnapshotOf(it.SNAPSHOT_OF);
		dbStorage.add(volumeInfo);
		dbStorage.commit();
	} catch(Throwable t) {
		t.printStackTrace();
		dbStorage.rollback();
	}	
}

//flush
System.out.println(Thread.currentThread().getStackTrace());
DatabaseUtil.closeAllEMFs();
oldDbStorage.close();
oldDbWalrus.close();
//the db will not sync to disk even after a close in some cases. Wait a bit.
System.out.println(Thread.currentThread().getStackTrace());
Thread.sleep(5000);
