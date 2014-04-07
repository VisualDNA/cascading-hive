/*
* Copyright (c) 2007-2014 Concurrent, Inc. All Rights Reserved.
*
* Project and contact information: http://www.cascading.org/
*
* This file is part of the Cascading project.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package cascading.tap.hive;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cascading.CascadingException;
import cascading.scheme.Scheme;
import cascading.scheme.hadoop.TextDelimited;
import cascading.tap.partition.Partition;
import cascading.tuple.Fields;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.MetaStoreUtils;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.SerDeInfo;
import org.apache.hadoop.hive.metastore.api.StorageDescriptor;
import org.apache.hadoop.hive.metastore.api.Table;

/**
 * HiveTableDescriptor encapsulates information about a table in Hive like the table name, column names, types,
 * partitioning etc. The class can convert the information to Hive specific objects or Cascading specific objects. It
 * acts as a translator of the concepts of a Hive table vs. the concepts of a Cascading Tap.
 */
public class HiveTableDescriptor implements Serializable
  {

  /** default DB in Hive. */
  public final static String HIVE_DEFAULT_DATABASE_NAME = MetaStoreUtils.DEFAULT_DATABASE_NAME;

  /** default delimiter in hive tables */
  public static final String HIVE_DEFAULT_DELIMITER = "\1";

  /** default input format used by Hive */
  public static final String HIVE_DEFAULT_INPUT_FORMAT_NAME = "org.apache.hadoop.mapred.TextInputFormat";

  /** default output format used by Hive */
  public static final String HIVE_DEFAULT_OUTPUT_FORMAT_NAME = "org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat";

  /** default serialization lib name */
  public static final String HIVE_DEFAULT_SERIALIZATION_LIB_NAME = HiveConf.ConfVars.HIVESCRIPTSERDE.defaultVal;

  /** columns to be used for partitioning */
  private String[] partitionKeys;

  /** field delimiter in the Hive table */
  private String delimiter;

  /** name of the hive table */
  private String tableName;

  /** name of the database */
  private String databaseName;

  /** names of the columns */
  private String[] columnNames;

  /** hive column types */
  private String[] columnTypes;

  /** Hive serialization library */
  private String serializationLib;

  /**
   * Constructs a new HiveTableDescriptor object.
   *
   * @param tableName   The table name.
   * @param columnNames Names of the columns.
   * @param columnTypes Hive types of the columns.
   */
  public HiveTableDescriptor( String tableName, String[] columnNames, String[] columnTypes )
    {
    this( HIVE_DEFAULT_DATABASE_NAME, tableName, columnNames, columnTypes, new String[]{}, HIVE_DEFAULT_DELIMITER,
      HIVE_DEFAULT_SERIALIZATION_LIB_NAME );
    }

  /**
   * Constructs a new HiveTableDescriptor object.
   *
   * @param tableName   The table name.
   * @param columnNames Names of the columns.
   * @param columnTypes Hive types of the columns.
   * @param partitionKeys The keys for partitioning the table.
   */
  public HiveTableDescriptor( String tableName, String[] columnNames, String[] columnTypes, String[] partitionKeys )
    {
    this( HIVE_DEFAULT_DATABASE_NAME, tableName, columnNames, columnTypes, partitionKeys, HIVE_DEFAULT_DELIMITER,
      HIVE_DEFAULT_SERIALIZATION_LIB_NAME );
    }

  /**
   * Constructs a new HiveTableDescriptor object.
   *
   * @param tableName   The table name.
   * @param columnNames Names of the columns.
   * @param columnTypes Hive types of the columns.
   * @param partitionKeys The keys for partitioning the table.
   * @param delimiter   The field delimiter of the Hive table.
   *
   */
  public HiveTableDescriptor( String tableName, String[] columnNames, String[] columnTypes, String[] partitionKeys, String delimiter )
    {
    this( HIVE_DEFAULT_DATABASE_NAME, tableName, columnNames, columnTypes, partitionKeys, delimiter,
      HIVE_DEFAULT_SERIALIZATION_LIB_NAME );
    }


  /**
   * Constructs a new HiveTableDescriptor object.
   *
   * @param databaseName The database name.
   * @param tableName   The table name.
   * @param columnNames Names of the columns.
   * @param columnTypes Hive types of the columns.
   */
  public HiveTableDescriptor( String databaseName, String tableName, String[] columnNames, String[] columnTypes )
    {
    this( databaseName, tableName, columnNames, columnTypes, new String[]{}, HIVE_DEFAULT_DELIMITER,
      HIVE_DEFAULT_SERIALIZATION_LIB_NAME );
    }

  /**
   * Constructs a new HiveTableDescriptor object.
   *
   * @param databaseName The database name.
   * @param tableName   The table name.
   * @param columnNames Names of the columns.
   * @param columnTypes Hive types of the columns.
   * @param partitionKeys The keys for partitioning the table.
   */
  public HiveTableDescriptor( String databaseName, String tableName, String[] columnNames, String[] columnTypes, String[] partitionKeys )
    {
    this( databaseName, tableName, columnNames, columnTypes, partitionKeys, HIVE_DEFAULT_DELIMITER, HIVE_DEFAULT_SERIALIZATION_LIB_NAME );
    }



  /**
   * Constructs a new HiveTableDescriptor object.
   *
   * @param databaseName     The database name.
   * @param tableName   The table name.
   * @param columnNames Names of the columns.
   * @param columnTypes Hive types of the columns.
   * @param partitionKeys The keys for partitioning the table.
   * @param delimiter   The field delimiter of the Hive table.
   *
   */
  public HiveTableDescriptor( String databaseName, String tableName, String[] columnNames, String[] columnTypes,
                              String[] partitionKeys, String delimiter )
    {
    this( databaseName, tableName, columnNames, columnTypes, partitionKeys, delimiter, HIVE_DEFAULT_SERIALIZATION_LIB_NAME );
    }

  /**
   * Constructs a new HiveTableDescriptor object.
   *
   * @param databaseName     The database name.
   * @param tableName        The table name
   * @param columnNames      Names of the columns
   * @param columnTypes      Hive types of the columns
   * @param delimiter        The field delimiter of the Hive table
   * @param serializationLib Hive serialization library.
   */
  public HiveTableDescriptor( String databaseName, String tableName, String[] columnNames, String[] columnTypes,
                              String[] partitionKeys, String delimiter,
                              String serializationLib )
    {
    if( tableName == null || tableName.isEmpty() )
      throw new IllegalArgumentException( "tableName cannot be null or empty" );
    if ( databaseName == null || tableName.isEmpty() )
      this.databaseName = HIVE_DEFAULT_DATABASE_NAME;
    else
      this.databaseName = databaseName;

    this.tableName = tableName;
    this.columnNames = columnNames;
    this.columnTypes = columnTypes;
    this.partitionKeys = partitionKeys;
    this.serializationLib = serializationLib;
    if( delimiter == null )
      this.delimiter = HIVE_DEFAULT_DELIMITER;
    else
      this.delimiter = delimiter;
    if ( isPartitioned() )
      verifyPartitionKeys();
    if( columnNames.length == 0 || columnTypes.length == 0 || columnNames.length != columnTypes.length )
      throw new IllegalArgumentException( "columnNames and columnTypes cannot be empty and must have the same size" );
    }

  /**
   * Private method to verify that all partition keys are also listed as column keys.
   */
  private void verifyPartitionKeys()
    {
    List names = Arrays.asList( columnNames );
    for( int index = 0; index < partitionKeys.length; index++ )
      {
      String key = partitionKeys[ index ];
      if( !names.contains( key ) )
        throw new IllegalArgumentException( String.format( "Given partition key '%s' not present in column names", key ) );
      }
    }


  /**
   * Converts the instance to a Hive Table object, which can be used with the MetaStore API.
   *
   * @return a new Table instance.
   */
  public Table toHiveTable()
    {
    Table table = new Table();
    table.setDbName( getDatabaseName() );
    table.setTableName( tableName );

    StorageDescriptor sd = new StorageDescriptor();
    List partitionColumns = Arrays.asList( partitionKeys );
    for( int index = 0; index < columnNames.length; index++ )
      {
      String columnName = columnNames[ index ];
      if (!partitionColumns.contains( columnName ))
        sd.addToCols( new FieldSchema( columnName, columnTypes[ index ], "created by Cascading" ) );
      }

    // TODO inspecting the Scheme for this might make sense. We might move this method elsewhere
    SerDeInfo serDeInfo = new SerDeInfo();
    serDeInfo.setSerializationLib( serializationLib );
    Map<String, String> serDeParameters = new HashMap<String, String>();
    serDeParameters.put( "serialization.format", getDelimiter() );
    serDeParameters.put( "field.delim", getDelimiter() );
    serDeInfo.setParameters( serDeParameters );

    sd.setSerdeInfo( serDeInfo );
    sd.setInputFormat( HIVE_DEFAULT_INPUT_FORMAT_NAME );
    sd.setOutputFormat( HIVE_DEFAULT_OUTPUT_FORMAT_NAME );
    table.setSd( sd );

    if ( isPartitioned() )
      {
      table.setPartitionKeys( getPartitionSchema() );
      table.setPartitionKeysIsSet( true );
      }

    return table;
    }

  private List<FieldSchema> getPartitionSchema()
    {
    List names = Arrays.asList( columnNames );
    List<FieldSchema> schema = new LinkedList<FieldSchema>();
    for( int i = 0; i < partitionKeys.length; i++ )
      {
      int index = names.indexOf( partitionKeys[ i ] );
      schema.add( new FieldSchema( columnNames[ index ], columnTypes[ index ], "" ) );
      }
    return schema;
    }

  /**
   * Returns a new Partition object to be used with a HivePartitionTap. If the table is not partitioned the method
   * will return null.
   * @return a new partition object or null.
   */
   public Partition getPartition()
     {
     if ( isPartitioned() )
       return new HivePartition( new Fields( getPartitionKeys() ) );
     throw new CascadingException( "non partitioned table cannot be used in a partitioned context" );
     }



   public Fields toFields()
    {
    if ( !isPartitioned() )
      return new Fields( columnNames );

    List names =  new ArrayList( Arrays.asList( columnNames ) );

    for ( String partName: getPartitionKeys())
      names.remove( partName );
    Comparable [] comparables = new Comparable[names.size()];
    return new Fields( (Comparable[]) names.toArray( comparables ) );

    }


  /**
   * Returns the path of the table within the warehouse directory.
   * @return The path of the table within the warehouse directory.
   */
  public String getFilesystemPath()
    {
    if ( getDatabaseName().equals( HIVE_DEFAULT_DATABASE_NAME ) )
      return getTableName();
    else
      return String.format( "%s.db/%s", getDatabaseName(), getTableName() );

    }


  /**
   * Converts the HiveTableDescriptor to a Scheme instance based on the information available.
   *
   * @return a new Scheme instance.
   */
  public Scheme toScheme()
    {
    // TODO add smarts to return the right thing.
    Scheme scheme = new TextDelimited( false, getDelimiter() );
    scheme.setSinkFields( toFields());
    return scheme;
    }

  public String[] getColumnNames()
    {
    return columnNames;
    }

  public String[] getColumnTypes()
    {
    return columnTypes;
    }

  public String getTableName()
    {
    return tableName;
    }

  public String getDatabaseName()
    {
    return databaseName;
    }

  public String getDelimiter()
    {
    return delimiter;
    }

  public String[] getPartitionKeys()
    {
    return partitionKeys;
    }

  public boolean isPartitioned()
    {
    return partitionKeys != null && partitionKeys.length > 0;
    }

  @Override
  public boolean equals( Object object )
    {
    if( this == object )
      {
      return true;
      }
    if( object == null || getClass() != object.getClass() )
      {
      return false;
      }

    HiveTableDescriptor that = (HiveTableDescriptor) object;

    if( !Arrays.equals( columnNames, that.columnNames ) )
      {
      return false;
      }
    if( !Arrays.equals( columnTypes, that.columnTypes ) )
      {
      return false;
      }
    if( databaseName != null ? !databaseName.equals( that.databaseName ) : that.databaseName != null )
      {
      return false;
      }
    if( delimiter != null ? !delimiter.equals( that.delimiter ) : that.delimiter != null )
      {
      return false;
      }
    if( !Arrays.equals( partitionKeys, that.partitionKeys ) )
      {
      return false;
      }
    if( serializationLib != null ? !serializationLib.equals( that.serializationLib ) : that.serializationLib != null )
      {
      return false;
      }
    if( tableName != null ? !tableName.equals( that.tableName ) : that.tableName != null )
      {
      return false;
      }

    return true;
    }

  @Override
  public int hashCode()
    {
    int result = partitionKeys != null ? Arrays.hashCode( partitionKeys ) : 0;
    result = 31 * result + ( delimiter != null ? delimiter.hashCode() : 0 );
    result = 31 * result + ( tableName != null ? tableName.hashCode() : 0 );
    result = 31 * result + ( databaseName != null ? databaseName.hashCode() : 0 );
    result = 31 * result + ( columnNames != null ? Arrays.hashCode( columnNames ) : 0 );
    result = 31 * result + ( columnTypes != null ? Arrays.hashCode( columnTypes ) : 0 );
    result = 31 * result + ( serializationLib != null ? serializationLib.hashCode() : 0 );
    return result;
    }

  @Override
  public String toString()
    {
    return "HiveTableDescriptor{" +
      "partitionKeys=" + Arrays.toString( partitionKeys ) +
      ", delimiter='" + delimiter + '\'' +
      ", tableName='" + tableName + '\'' +
      ", databaseName='" + databaseName + '\'' +
      ", columnNames=" + Arrays.toString( columnNames ) +
      ", columnTypes=" + Arrays.toString( columnTypes ) +
      ", serializationLib='" + serializationLib + '\'' +
      '}';
    }
  }
