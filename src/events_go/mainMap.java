/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package events_go;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.io.IOException;
import static java.lang.Math.cos;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapPolygon;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

/**
 *
 * @author Sergey
 */
public class mainMap extends javax.swing.JFrame {

    JMapViewer map;
    
    private static final String URL         = "https://maps.googleapis.com/maps/api/geocode/xml";
    private static final String DEFAULT_KEY = "AIzaSyBTvVs2Lcn_Ul9NjmSDFlxw3CbYhInszxI"; 
    
    class coordinateClass { 
        double lat;
        double lon;
    }
    
    coordinateClass searchCoord = new coordinateClass();
    
    private String driver_db;
    private String url_db;
    private String login_db;
    private String pass_db;
    
    String[] head_event = {"ID_EVENT","NAME","START DATE","END DATE","COST","TYPE"};
    DefaultTableModel tableModelEvent = new DefaultTableModel(null, head_event);
    String[] head_place = {"ID_PLACE","NAME","ADDRESS"};
    DefaultTableModel tableModelPlace = new DefaultTableModel(null, head_place);
    String[] head_person = {"ID_PERSON","NAME","FAMILY","AGE","ADDRESS"};
    DefaultTableModel tableModelPerson = new DefaultTableModel(null, head_person);
    String[] head_object = {"ID_OBJECT","NAME","TYPE","LAT","LON","AREA"};
    DefaultTableModel tableModelObject = new DefaultTableModel(null, head_object);
    
    
    /**
     * Creates new form mainMap
     */
    
    public void setConn_params (String driver,String url,String login,String pass) {
        driver_db = driver;
        url_db = url;
        login_db = login;
        pass_db = pass;
    }
     
    public mainMap() {
        initComponents();
        map = new JMapViewer();
        map.setTileSource(new OsmTileSource.Mapnik());
        map.setTileLoader(new OsmTileLoader(map));
        map.setTileGridVisible(map.isTileGridVisible());
        jPanel_map.setLayout(new BorderLayout());
        jPanel_map.add(map, BorderLayout.CENTER);       
        showZoomControls.setSelected(map.getZoomControlsVisible());
        showTileGrid.setSelected(map.isTileGridVisible());
    }
    
    private void LoadPointsEvent(String ID_event) {
         try {
            
            int counter = 0;
            float lon = 0;
            float lat = 0;

            map.removeAllMapMarkers();
        
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment
            String query_str = "Select DISTINCT (go.id_object), SDO_GEOM.SDO_CENTROID(go.shape, m.diminfo).sdo_point.x as LAT, SDO_GEOM.SDO_CENTROID(go.shape, m.diminfo).sdo_point.y as LON\n" +
                                " FROM user_sdo_geom_metadata m, geo_object go\n" +
                                " INNER JOIN geo_place_object po on po.id_object = go.id_object\n" +
                                " INNER JOIN geo_place_event pe on pe.id_place = po.id_place\n" +
                                " WHERE pe.id_event = "+ID_event; //Create query
            
            ResultSet query_result = stmt.executeQuery(query_str); //Get result
            
            while(query_result.next()){
                lat = query_result.getFloat("LON");
                lon = query_result.getFloat("LAT");
                
                map.addMapMarker(new MapMarkerDot(lat,lon));
                
                counter++;
            }
            
            setMapCenter(lat, lon, 17);
                                
            db_connect.close();
            stmt.close();
            query_result.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(load_shape.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
    
    private void LoadPointsPlace(String ID_place) {
         try {
            
            int counter = 0;
            float lon = 0;
            float lat = 0;

            map.removeAllMapMarkers();
        
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment
            String query_str = "Select DISTINCT (go.id_object), SDO_GEOM.SDO_CENTROID(go.shape, m.diminfo).sdo_point.x as LAT, SDO_GEOM.SDO_CENTROID(go.shape, m.diminfo).sdo_point.y as LON\n" +
                                " FROM user_sdo_geom_metadata m, geo_object go\n" +
                                " INNER JOIN geo_place_object po on po.id_object = go.id_object\n" +
                                " WHERE po.id_place = "+ID_place; //Create query
            
            ResultSet query_result = stmt.executeQuery(query_str); //Get result
            
            while(query_result.next()){
                lat = query_result.getFloat("LON");
                lon = query_result.getFloat("LAT");
                
                map.addMapMarker(new MapMarkerDot(lat,lon));
                
                counter++;
            }
            
            
            
            setMapCenter(lat, lon, 17);
                                
            db_connect.close();
            stmt.close();
            query_result.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(load_shape.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
    
    private void LoadPointsObject(String ID_object) {
         try {
            
            int counter = 0;
            float lon = 0;
            float lat = 0;

            map.removeAllMapMarkers();
        
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment
            String query_str = "Select DISTINCT (go.id_object), SDO_GEOM.SDO_CENTROID(go.shape, m.diminfo).sdo_point.x as LAT, SDO_GEOM.SDO_CENTROID(go.shape, m.diminfo).sdo_point.y as LON\n" +
                                " FROM user_sdo_geom_metadata m, geo_object go\n" +
                                " WHERE go.id_object = "+ID_object; //Create query
            
            ResultSet query_result = stmt.executeQuery(query_str); //Get result
            
            while(query_result.next()){
                lat = query_result.getFloat("LON");
                lon = query_result.getFloat("LAT");
                
                map.addMapMarker(new MapMarkerDot(lat,lon));
                
                counter++;
            }
            
            setMapCenter(lat, lon, 17);
                                
            db_connect.close();
            stmt.close();
            query_result.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(load_shape.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
    
    private void setMapCenter(double lat, double lon, int zoom) {
        ICoordinate center = map.getPosition();
        center.setLat(lat);
        center.setLon(lon);
        map.setDisplayPosition(center, zoom);
    }
        
    private void LoadPointsPersone(String ID_persone) {
         try {
            
            int counter = 0;
            coordinateClass coord = new coordinateClass();

            map.removeAllMapMarkers();
        
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment
            String query_str = "Select *" +
                                " FROM GEO_PERSONE" +
                                " WHERE id_persone = " + ID_persone; //Create query
            
            ResultSet query_result = stmt.executeQuery(query_str); //Get result
            
            while(query_result.next()){
                String address = query_result.getString("ADDRESS");           
                coord = getCoordinate(address);
                map.addMapMarker(new MapMarkerDot(coord.lat,coord.lon));
                setMapCenter(coord.lat,coord.lon, 17);
                counter++;
            }
                        
            db_connect.close();
            stmt.close();
            query_result.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(load_shape.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
        
    private String getAddress(coordinateClass coord) {
        String address = "";
        try {
            String url = (URL + "?latlng=" + String.valueOf(coord.lat)+","+ String.valueOf(coord.lon) + "&key=" + DEFAULT_KEY);
            
            Document doc = Jsoup
                    .connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36")
                    .timeout(20000)
                    .get();
            
            Elements links = doc.select("formatted_address");     
            
            address = links.get(0).text();

        } catch (IOException ex) {
            Logger.getLogger(personeClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        return address;
    }
    
    private coordinateClass getCoordinate(String addres_str) {
        coordinateClass coord = new coordinateClass();
        
        try {
            String url = (URL + "?address=" + addres_str + "&key=" + DEFAULT_KEY);
            
            Document doc = Jsoup
                    .connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36")
                    .timeout(20000)
                    .get();
            
            Elements links_lat = doc.select("location lat");
            Elements links_lon = doc.select("location lng");
            
            coord.lat = Float.parseFloat(links_lat.get(0).text());
            coord.lon = Double.parseDouble(links_lon.get(0).text());
            
            return coord;
        } catch (IOException ex) {
            Logger.getLogger(personeClass.class.getName()).log(Level.SEVERE, null, ex);
            coord.lat = 51.8311104;
            coord.lon = 12.2429261;   
            return coord;
        }
    }
    
    private void Load_events() {
         try {
            
            int counter = 0;
            tableModelEvent.setRowCount(0);
        
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment
            String query_str = "SELECT EV.ID_EVENT, EV.NAME, EV.START_DATATIME, EV.END_DATATIME, EV.COST, ET.NAME as TYPE_NAME"
                    + " FROM GEO_EVENT EV"
                    + " INNER JOIN GEO_EVENT_TYPE ET ON EV.ID_TYPE = ET.ID_TYPE"; //Create query
            
            ResultSet query_result = stmt.executeQuery(query_str); //Get result
            
            while(query_result.next()){
                String ID = String.valueOf(query_result.getInt("ID_EVENT"));
                String name = query_result.getString("NAME");
                String start_data = query_result.getTimestamp("START_DATATIME").toString();
                String end_data = query_result.getTimestamp("END_DATATIME").toString();
                String cost = String.valueOf(query_result.getFloat("COST")).replace('.', ',');
                String name_type = query_result.getString("TYPE_NAME");
                String[] row = {ID,name,start_data,end_data,cost,name_type};
                tableModelEvent.addRow(row);
                counter++;
            }
           
                    
            db_connect.close();
            stmt.close();
            query_result.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(load_shape.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void LoadPlace(String ID_event) {
        try {
            
            int counter = 0;
            tableModelPlace.setRowCount(0);
        
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment
            String query_str = "SELECT PL.ID_PLACE, PL.NAME, PL.ADDRESS"
                    + " FROM GEO_PLACE PL"
                    + " INNER JOIN GEO_PLACE_EVENT PE ON PL.ID_PLACE = PE.ID_PLACE"
                    + " WHERE PE.ID_EVENT ='" + ID_event +"'"; //Create query
            
            ResultSet query_result = stmt.executeQuery(query_str); //Get result
            
            while(query_result.next()){
                String ID = String.valueOf(query_result.getInt("ID_PLACE"));
                String name = query_result.getString("NAME");
                String address = query_result.getString("ADDRESS");
                
                String[] row = {ID,name,address};
                tableModelPlace.addRow(row);
                counter++;
            }
            
            db_connect.close();
            stmt.close();
            query_result.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(load_shape.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
    private void LoadPerson(String ID_event) {
        try {
            
            int counter = 0;
            tableModelPerson.setRowCount(0);
        
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment
            String query_str = "SELECT PE.ID_PERSONE, PE.NAME, PE.FAMILY_NAME, PE.ADDRESS, PE.AGE"
                    + " FROM GEO_PERSONE PE"
                    + " INNER JOIN GEO_PERSONE_EVENT EP ON PE.ID_PERSONE = EP.ID_PERSONE"
                    + " WHERE EP.ID_EVENT ='" + ID_event +"'"; //Create query
            
            ResultSet query_result = stmt.executeQuery(query_str); //Get result
            
            while(query_result.next()){
                String ID = String.valueOf(query_result.getInt("ID_PERSONE"));
                String name = query_result.getString("NAME");
                String family = query_result.getString("FAMILY_NAME");
                String address = query_result.getString("ADDRESS");
                String age = String.valueOf(query_result.getInt("AGE"));
                
                String[] row = {ID,name,family,age,address};
                tableModelPerson.addRow(row);
                counter++;
            }
            
            db_connect.close();
            stmt.close();
            query_result.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(load_shape.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void LoadObjectsPlace(String ID_place) {
        try {
            
            int counter = 0;
            tableModelObject.setRowCount(0);
            
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment

            //Builds
            String query_str = "SELECT BM.ID_OBJECT,BM.type,BM.name, SDO_GEOM.SDO_CENTROID(BM.shape, m.diminfo).sdo_point.x as LAT, SDO_GEOM.SDO_CENTROID(BM.shape, m.diminfo).sdo_point.y as LON, SDO_GEOM.SDO_AREA(BM.shape, 0.005)*1000000000 as area" 
                            +" FROM user_sdo_geom_metadata m, GEO_OBJECT BM" 
                            +" INNER JOIN GEO_PLACE_OBJECT PB ON BM.ID_OBJECT = PB.ID_OBJECT"
                            +" INNER JOIN GEO_PLACE PL ON  PB.ID_PLACE = PL.ID_PLACE"
                            +" WHERE (m.table_name = 'GEO_BUILD_MAP' AND m.column_name = 'SHAPE') and (PL.ID_PLACE='" + ID_place + "')"; //Create query
            
            ResultSet query_result = stmt.executeQuery(query_str); //Get result
            
            while(query_result.next()){
                String ID = String.valueOf(query_result.getInt("ID_OBJECT"));
                String name = query_result.getString("NAME");
                String type = query_result.getString("TYPE");
                String lat = String.valueOf(query_result.getFloat("LAT"));
                String lon = String.valueOf(query_result.getFloat("LON"));
                String area = String.valueOf(query_result.getFloat("AREA"));
                String[] row = {ID,name,type,lat,lon,area};
                
                tableModelObject.addRow(row);
                counter++;
            }
            
            
            map.removeAllMapMarkers();
            
            for (int i=0; i<counter; i++) {
                Float lat = Float.valueOf(tableModelObject.getValueAt(i, 3).toString());
                Float lon = Float.valueOf(tableModelObject.getValueAt(i, 4).toString());
                map.addMapMarker(new MapMarkerDot(lon, lat));
            }
            
                    
            db_connect.close();
            stmt.close();
            query_result.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(load_shape.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel_map = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jButton_address = new javax.swing.JButton();
        jTextField_address = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton_radius = new javax.swing.JButton();
        jTextField_radius = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel_coord = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable_events = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable_persones = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable_places = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable_objects = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        showZoomControls = new javax.swing.JCheckBox();
        showTileGrid = new javax.swing.JCheckBox();
        jButton_zoom_plus = new javax.swing.JButton();
        jButton_zoom_minus = new javax.swing.JButton();
        jButton_zoom_reset = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jPanel_map.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel_mapLayout = new javax.swing.GroupLayout(jPanel_map);
        jPanel_map.setLayout(jPanel_mapLayout);
        jPanel_mapLayout.setHorizontalGroup(
            jPanel_mapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel_mapLayout.setVerticalGroup(
            jPanel_mapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Search"));

        jButton_address.setText("Search");
        jButton_address.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_addressActionPerformed(evt);
            }
        });

        jLabel1.setText("Search address:");

        jLabel2.setText("Search events in R:");

        jButton_radius.setText("Search");
        jButton_radius.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_radiusActionPerformed(evt);
            }
        });

        jTextField_radius.setText("300");
        jTextField_radius.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField_radiusActionPerformed(evt);
            }
        });

        jLabel7.setText("Coord:");

        jLabel_coord.setText("lat: 0 lon: 0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(6, 6, 6)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jTextField_address, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton_address, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jTextField_radius, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_radius)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel_coord, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField_address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton_address))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jButton_radius)
                    .addComponent(jTextField_radius, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel_coord))
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Information"));

        jTable_events.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable_events.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable_eventsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable_events);

        jTable_persones.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable_persones.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable_personesMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTable_persones);

        jTable_places.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable_places.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable_placesMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(jTable_places);

        jLabel4.setText("Persones");

        jLabel5.setText("Places");

        jLabel6.setText("Objects");

        jTable_objects.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable_objects.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable_objectsMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(jTable_objects);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane4)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        showZoomControls.setText("Show zoom controls");
        showZoomControls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showZoomControlsActionPerformed(evt);
            }
        });

        showTileGrid.setText("Tile grid visible");
        showTileGrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTileGridActionPerformed(evt);
            }
        });

        jButton_zoom_plus.setText("Zoom +");
        jButton_zoom_plus.setMaximumSize(new java.awt.Dimension(69, 20));
        jButton_zoom_plus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_zoom_plusActionPerformed(evt);
            }
        });

        jButton_zoom_minus.setText("Zoom -");
        jButton_zoom_minus.setMaximumSize(new java.awt.Dimension(69, 20));
        jButton_zoom_minus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_zoom_minusActionPerformed(evt);
            }
        });

        jButton_zoom_reset.setText("Zoom reset");
        jButton_zoom_reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_zoom_resetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(showZoomControls)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showTileGrid)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_zoom_plus, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_zoom_minus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_zoom_reset)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(showZoomControls)
                .addComponent(showTileGrid, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton_zoom_plus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton_zoom_minus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton_zoom_reset))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel_map, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jPanel_map, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void showZoomControlsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showZoomControlsActionPerformed
        map.setZoomContolsVisible(showZoomControls.isSelected());
    }//GEN-LAST:event_showZoomControlsActionPerformed

    private void showTileGridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTileGridActionPerformed
        map.setTileGridVisible(showTileGrid.isSelected());
    }//GEN-LAST:event_showTileGridActionPerformed

    private void jButton_addressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_addressActionPerformed
        coordinateClass coord = new coordinateClass();
        if (!"".equals(jTextField_address.getText())) {
            coord = getCoordinate(jTextField_address.getText());
            searchCoord = coord;
            setMapCenter(coord.lat, coord.lon, map.getZoom());
            setMapPoint(coord);
            jLabel_coord.setText("lat: "+String.valueOf(coord.lat)+"  lon: "+String.valueOf(coord.lon));
        }
    }//GEN-LAST:event_jButton_addressActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        jTable_events.setModel(tableModelEvent);
        jTable_persones.setModel(tableModelPerson);
        jTable_places.setModel(tableModelPlace);
        jTable_objects.setModel(tableModelObject);  
        Load_events();
        if (jTable_events.getRowCount() > 0) {
            jTable_events.setRowSelectionInterval(0, 0);
            LoadPlace(jTable_events.getValueAt(jTable_events.getSelectedRow(),0).toString());
            if (jTable_places.getRowCount() > 0) {
                jTable_places.setRowSelectionInterval(0, 0);
                LoadObjectsPlace(jTable_places.getValueAt(jTable_places.getSelectedRow(),0).toString());
                LoadPointsEvent(jTable_events.getValueAt(jTable_events.getSelectedRow(), 0).toString());
            }
            LoadPerson(jTable_events.getValueAt(jTable_events.getSelectedRow(),0).toString());
            if (jTable_persones.getRowCount() > 0) {
                jTable_persones.setRowSelectionInterval(0, 0);
            } 
        }
        
         new DefaultMapController(map){
            @Override
            public void mouseClicked(MouseEvent e) {
                coordinateClass coord = new coordinateClass();
                coord.lat = map.getPosition(e.getPoint()).getLat();
                coord.lon = map.getPosition(e.getPoint()).getLon();
                searchCoord = coord;
                jLabel_coord.setText("lat: "+String.valueOf(coord.lat)+"  lon: "+String.valueOf(coord.lon));
                jTextField_address.setText(getAddress(coord));
                System.out.println(map.getPosition(e.getPoint()));
            }
        };
         
    }//GEN-LAST:event_formWindowOpened

    private void jTable_eventsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable_eventsMouseClicked
        LoadPointsEvent(jTable_events.getValueAt(jTable_events.getSelectedRow(), 0).toString());
        LoadPlace(jTable_events.getValueAt(jTable_events.getSelectedRow(),0).toString());
        LoadPerson(jTable_events.getValueAt(jTable_events.getSelectedRow(),0).toString());
        if (jTable_places.getRowCount() > 0) {
            jTable_places.setRowSelectionInterval(0, 0);
            LoadObjectsPlace(jTable_places.getValueAt(jTable_places.getSelectedRow(),0).toString());
            LoadPointsEvent(jTable_events.getValueAt(jTable_events.getSelectedRow(), 0).toString());
        }
    }//GEN-LAST:event_jTable_eventsMouseClicked

    private void jTable_personesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable_personesMouseClicked
        LoadPointsPersone(jTable_persones.getValueAt(jTable_persones.getSelectedRow(),0).toString());
    }//GEN-LAST:event_jTable_personesMouseClicked

    private void jTable_placesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable_placesMouseClicked
        LoadPointsPlace(jTable_places.getValueAt(jTable_places.getSelectedRow(),0).toString());
        if (jTable_places.getRowCount() > 0) {
            LoadObjectsPlace(jTable_places.getValueAt(jTable_places.getSelectedRow(),0).toString());
            LoadPointsPlace(jTable_places.getValueAt(jTable_places.getSelectedRow(),0).toString());
        }
    }//GEN-LAST:event_jTable_placesMouseClicked

    private void jTable_objectsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable_objectsMouseClicked
        LoadPointsObject(jTable_objects.getValueAt(jTable_objects.getSelectedRow(),0).toString());
    }//GEN-LAST:event_jTable_objectsMouseClicked

    private void jButton_radiusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_radiusActionPerformed
        try {
            
            int counter = 0;
            String radius = jTextField_radius.getText();
            double delta = convert_coord(Float.valueOf(radius));
            String lat = String.valueOf(searchCoord.lat);
            String lon = String.valueOf(searchCoord.lon);
            
            tableModelEvent.setRowCount(0);
            
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment

            //Builds
            String query_str = "SELECT DISTINCT (ev.id_event), EV.ID_EVENT, EV.NAME, EV.START_DATATIME, EV.END_DATATIME, EV.COST, ET.NAME as TYPE_NAME\n" +
                                "FROM user_sdo_geom_metadata m, geo_event ev\n" +
                                "INNER JOIN geo_place_event pe on pe.id_event = ev.id_event\n" +
                                "INNER JOIN geo_place_object po on po.id_place = pe.id_place\n" +
                                "INNER JOIN geo_object ob on ob.id_object = po.id_object\n" +
                                "INNER JOIN GEO_EVENT_TYPE ET ON EV.ID_TYPE = ET.ID_TYPE\n" +
                                "WHERE SDO_GEOM.WITHIN_DISTANCE(ob.shape, m.diminfo, "+String.valueOf(delta)+",\n" +
                                "     (\n" +
                                "	 mdsys.sdo_geometry(2001,\n" +
                                "		NULL,\n" +
                                "		mdsys.sdo_point_type("+lon+","+lat+",0),\n" +
                                "		NULL,\n" +
                                "		NULL)\n" +
                                "	 )\n" +
                                "	 , m.diminfo) = 'TRUE'"; //Create query
            
            ResultSet query_result = stmt.executeQuery(query_str); //Get result
            
            while(query_result.next()){
                String ID = String.valueOf(query_result.getInt("ID_EVENT"));
                String name = query_result.getString("NAME");
                String start_data = query_result.getTimestamp("START_DATATIME").toString();
                String end_data = query_result.getTimestamp("END_DATATIME").toString();
                String cost = String.valueOf(query_result.getFloat("COST")).replace('.', ',');
                String name_type = query_result.getString("TYPE_NAME");
                String[] row = {ID,name,start_data,end_data,cost,name_type};
                tableModelEvent.addRow(row);
                counter++;
            }
            
            setMapPoint(searchCoord);
                                
            db_connect.close();
            stmt.close();
            query_result.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(load_shape.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton_radiusActionPerformed

    private void jTextField_radiusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField_radiusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField_radiusActionPerformed

    private void jButton_zoom_resetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_zoom_resetActionPerformed
        setMapCenter(searchCoord.lat, searchCoord.lon, 17);
    }//GEN-LAST:event_jButton_zoom_resetActionPerformed

    private void jButton_zoom_plusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_zoom_plusActionPerformed
        if (map.getZoom() < 19) {
            map.setZoom(map.getZoom()+1);
        }
    }//GEN-LAST:event_jButton_zoom_plusActionPerformed

    private void jButton_zoom_minusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_zoom_minusActionPerformed
        if (map.getZoom() > 2) {
            map.setZoom(map.getZoom()-1);
        }
    }//GEN-LAST:event_jButton_zoom_minusActionPerformed
            
    private double convert_coord(float radius) {
	float rlat = 0;
	float m = (float) (111132.09  - 566.05 * cos(2 * rlat) + 1.2 * cos(4 * rlat));
	float dlat = radius / m;
	return dlat;
    }
    
    private void setMapPoint(coordinateClass coord) {
        map.removeAllMapMarkers();
        map.addMapMarker(new MapMarkerDot(coord.lat, coord.lon));
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(mainMap.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(mainMap.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(mainMap.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(mainMap.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new mainMap().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_address;
    private javax.swing.JButton jButton_radius;
    private javax.swing.JButton jButton_zoom_minus;
    private javax.swing.JButton jButton_zoom_plus;
    private javax.swing.JButton jButton_zoom_reset;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel_coord;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel_map;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable_events;
    private javax.swing.JTable jTable_objects;
    private javax.swing.JTable jTable_persones;
    private javax.swing.JTable jTable_places;
    private javax.swing.JTextField jTextField_address;
    private javax.swing.JTextField jTextField_radius;
    private javax.swing.JCheckBox showTileGrid;
    private javax.swing.JCheckBox showZoomControls;
    // End of variables declaration//GEN-END:variables
}
