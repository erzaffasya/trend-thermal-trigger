
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.util.HashMap;
import java.util.Map;
import java.awt.image.BufferedImage;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.PrintQuality;
import javax.print.attribute.standard.Sides;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PrintThermal extends JFrame {

    private JComboBox<String> printerComboBox;
    private JTextField textField;
    private String selectedPrinter;
    private String nomorPenjualan;
    JSONObject dataPenjualan = new JSONObject();
    JSONObject dataPerusahaan = new JSONObject();
    JSONArray dataPenjualanDetail = new JSONArray();
    PrintService service;
    Graphics2D g2d;
    private BufferedImage img = null;

    public void getAntrian() {
        try {
            // Create the URL object with the API endpoint
            URL url = new URL("https://sap.trendvariasi.id/api/antrian-printer");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String jsonResponse = response.toString();

                JSONObject jsonObj = new JSONObject(jsonResponse);
                // jsonObj.isEmpty();
                if (!jsonObj.isEmpty()) {
                    this.getData(jsonObj.getJSONObject("data").getString("nomor"));
                    System.out.println("Ada Datanya ni");
                }
                dataPenjualan = jsonObj.getJSONObject("data");
            } else {
                System.out.println("HTTP Request Failed with Error Code: " + responseCode);
            }
            // Close the connection
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getData(String nomor) {
        try {
            // Create the URL object with the API endpoint

            String replacedString = nomor.replace("/", "-");
            URL url = new URL("https://sap.trendvariasi.id/api/penjualan/" + replacedString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String jsonResponse = response.toString();

                JSONObject jsonArray = new JSONObject(jsonResponse);
                dataPenjualan = jsonArray.getJSONObject("data");
                dataPerusahaan = jsonArray.getJSONObject("perusahaan");
                dataPenjualanDetail = jsonArray.getJSONArray("detail");
                this.NotaAll();
                // dataPerusahaan.getString("nama");
            } else {
                System.out.println("HTTP Request Failed with Error Code: " + responseCode);
            }
            // Close the connection
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void NotaAll() {
        System.out.println(selectedPrinter);

        if (findPrintService(selectedPrinter)) {

            PrintRequestAttributeSet attributesSet = new HashPrintRequestAttributeSet();
            attributesSet.add(new MediaPrintableArea(0, 0, 80, 297, MediaPrintableArea.MM));
            attributesSet.add(PrintQuality.HIGH);
            attributesSet.add(Sides.ONE_SIDED);

            Font font = new Font("Times New Roman", Font.PLAIN, 9);

            // serviceThermal = new ServiceThermal();
            try {
                // Create print job
                PrinterJob printerJob = PrinterJob.getPrinterJob();
                printerJob.setPrintService(service);
                String title = "RINCIAN TRANSAKSI";
                printerJob.setPrintable((graphics, pageFormat, pageIndex) -> {
                    if (pageIndex == 0) {
                        g2d = (Graphics2D) graphics;

                        // font = font;
                        line = 10;
                        width = (int) pageFormat.getImageableWidth();

                        Map<TextAttribute, Object> attributes = new HashMap<>();
                        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
                        Font fontFormat = new Font("Times New Roman", Font.PLAIN, 12);
                        Font fontSubTitleFormat = new Font("Times New Roman", Font.PLAIN, 9);
                        Font fontTitle = fontFormat.deriveFont(attributes);
                        Font fontSubTitle = fontSubTitleFormat.deriveFont(attributes);
                        Font fontBoldFormat = new Font("Times New Roman", Font.PLAIN, 9);
                        Font fontBold = fontBoldFormat.deriveFont(attributes);

                        this.drawText(g2d, "RINCIAN TRANSAKSI", "center", fontTitle);
                        this.drawText(g2d, "BUKAN BUKTI PEMBELIAN/PEMBAYARAN", "center",
                                line, fontSubTitle);
                        this.drawText(g2d, dataPerusahaan.getString("alamat"), "center");
                        this.drawText(g2d, dataPerusahaan.getString("no_hp"), "center");

                        this.dash(g2d);

                        this.drawLeftRight(g2d, new SimpleDateFormat("d MMMM y", new java.util.Locale("id"))
                                .format(new Date()), new SimpleDateFormat("hh:mm:ss").format(new Date()),
                                line, font);

                        this.dash(g2d);

                        this.drawText(g2d,
                                "PELANGGAN : " + dataPenjualan.getString("id_customer") + " - "
                                        + dataPenjualan.getString("customer"));

                        this.drawText(g2d, "MOBIL : " + dataPenjualan.getString("mobil") + " - "
                                + dataPenjualan.getString("nopol"));

                        this.dash(g2d);

                        this.drawLeftRight(g2d, dataPenjualan.getString("rincian_transaksi"),
                                dataPenjualan.getString("pegawai"),
                                line, font);

                        this.dash(g2d);

                        for (int i = 0; i < dataPenjualanDetail.length(); i++) {
                            JSONObject dataObj = new JSONObject(dataPenjualanDetail.get(i).toString());
                            this.drawText(g2d, dataObj.getString("id_barang") + " " + dataObj.getString("nama_barang"));

                            this.drawLeftCenterRight(g2d, "Rp. ",
                                    new java.text.DecimalFormat("#,##0")
                                            .format(dataObj.getDouble("harga")) + "x"
                                            + dataObj.getDouble("qty"),
                                    "Rp. "
                                            + new java.text.DecimalFormat("#,##0")
                                                    .format(Double.valueOf(dataObj.getDouble("total"))),
                                    line,
                                    font);
                        }
                        this.dash(g2d);

                        this.drawLeftRight(g2d, "SUBTOTAL", new java.text.DecimalFormat("#,##0")
                                .format(dataPenjualan.getDouble("total")),
                                line, font);
                        line = line + 5;

                        this.drawLeftRight(g2d, "DISKON", new java.text.DecimalFormat("#,##0")
                                .format(dataPenjualan.getDouble("total_discount")),
                                line, font);
                        line = line + 5;

                        this.drawLeftRight(g2d, "TOTAL", new java.text.DecimalFormat("#,##0")
                                .format(dataPenjualan.getDouble("grandtotal")),
                                line, fontBold);

                        return Printable.PAGE_EXISTS;
                    } else {
                        return Printable.NO_SUCH_PAGE;
                    }
                });
                printerJob.print(attributesSet);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Printer not found.");
        }
    }

    public Boolean findPrintService(String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

        for (PrintService printService : printServices) {
            if (printService.getName().equalsIgnoreCase(printerName)) {
                service = printService;
                return true;
            }
        }
        return false;
    }

    private Integer line;
    private Integer linePending;
    private Integer width;
    private Integer height;
    private Font font;

    public void drawText(Graphics g, String text, String align, Integer l, Font font) {

        line = l;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(font);

        String[] words = text.split(" ");

        // Variabel untuk menyimpan teks setiap baris
        Map<Integer, String> texts = new HashMap<>();

        // Menggabungkan kata-kata menjadi baris-baris
        Integer nextLine = 0;
        for (String word : words) {

            String tempLine = texts.get(line) == null ? word : texts.get(line) + " " + word;
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(tempLine, g2d);
            if (bounds.getWidth() > width) {
                tempLine = word;
                line += (int) bounds.getHeight();
                texts.put(line, tempLine);
            } else {
                texts.put(line, tempLine);
            }
            nextLine = (int) bounds.getHeight();
        }
        line += nextLine;

        texts.entrySet().forEach((entry) -> {
            int x = 0;
            Integer y = entry.getKey();
            String val = entry.getValue();
            if (null != align) {
                switch (align) {
                    case "center":
                        x = (int) ((width) / 2 - g2d.getFontMetrics().getStringBounds(val, g2d).getWidth() / 2);
                        break;
                    case "left":
                        x = (int) 10;
                        break;
                    case "right":
                        x = (int) ((width) - g2d.getFontMetrics().getStringBounds(val, g2d).getWidth());
                        break;
                    default:
                        x = (int) 0;
                        break;
                }
            }

            g2d.drawString(val.trim(), x + 5, y);
        });
    }

    public void drawText(Graphics g, String text, String align, Font font) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(font);

        String[] words = text.split(" ");

        // Variabel untuk menyimpan teks setiap baris
        Map<Integer, String> texts = new HashMap<>();

        // Menggabungkan kata-kata menjadi baris-baris
        Integer nextLine = 0;
        for (String word : words) {

            String tempLine = texts.get(line) == null ? word : texts.get(line) + " " + word;
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(tempLine, g2d);
            if (bounds.getWidth() > width) {
                tempLine = word;
                line += (int) bounds.getHeight();
                texts.put(line, tempLine);
            } else {
                texts.put(line, tempLine);
            }
            nextLine = (int) bounds.getHeight();
        }
        line += nextLine;

        texts.entrySet().forEach((entry) -> {
            int x = 0;
            Integer y = entry.getKey();
            String val = entry.getValue();
            if (null != align) {
                switch (align) {
                    case "center":
                        x = (int) ((width) / 2 - g2d.getFontMetrics().getStringBounds(val, g2d).getWidth() / 2);
                        break;
                    case "left":
                        x = (int) 10;
                        break;
                    case "right":
                        x = (int) ((width) - g2d.getFontMetrics().getStringBounds(val, g2d).getWidth());
                        break;
                    default:
                        x = (int) 0;
                        break;
                }
            }

            g2d.drawString(val.trim(), x + 5, y);
        });
    }

    public void drawText(Graphics g, String text, String align) {

        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(font);

        String[] words = text.split(" ");

        // Variabel untuk menyimpan teks setiap baris
        Map<Integer, String> texts = new HashMap<>();

        // Menggabungkan kata-kata menjadi baris-baris
        Integer nextLine = 0;
        for (String word : words) {

            String tempLine = texts.get(line) == null ? word : texts.get(line) + " " + word;
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(tempLine, g2d);
            if (bounds.getWidth() > width - 10) {
                tempLine = word;
                line += (int) bounds.getHeight();
                texts.put(line, tempLine);
            } else {
                texts.put(line, tempLine);
            }
            nextLine = (int) bounds.getHeight();
        }
        line += nextLine;

        texts.entrySet().forEach((entry) -> {
            int x = 0;
            Integer y = entry.getKey();
            String val = entry.getValue();
            if (null != align) {
                switch (align) {
                    case "center":
                        x = (int) ((width) / 2 - g2d.getFontMetrics().getStringBounds(val, g2d).getWidth() / 2);
                        break;
                    case "left":
                        x = (int) 10;
                        break;
                    case "right":
                        x = (int) ((width) - g2d.getFontMetrics().getStringBounds(val, g2d).getWidth());
                        break;
                    default:
                        x = (int) 0;
                        break;
                }
            }

            g2d.drawString(val.trim(), x + 7, y);
        });
    }

    public void drawText(Graphics g, String text, String align, Integer ml, Integer l, Font font) {

        line = l;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(font);

        String[] words = text.split(" ");

        // Variabel untuk menyimpan teks setiap baris
        Map<Integer, String> texts = new HashMap<>();

        // Menggabungkan kata-kata menjadi baris-baris
        Integer nextLine = 0;
        for (String word : words) {

            String tempLine = texts.get(line) == null ? word : texts.get(line) + " " + word;
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(tempLine, g2d);
            if (bounds.getWidth() > width) {
                tempLine = word;
                line += (int) bounds.getHeight();
                texts.put(line, tempLine);
            } else {
                texts.put(line, tempLine);
            }
            nextLine = (int) bounds.getHeight();
        }
        line += nextLine;

        texts.entrySet().forEach((entry) -> {
            int x = ml;
            Integer y = entry.getKey();
            String val = entry.getValue();
            if (null != align) {
                switch (align) {
                    case "center":
                        x += (int) ((width) / 2 - g2d.getFontMetrics().getStringBounds(val, g2d).getWidth() / 2);
                        break;
                    case "left":
                        x += (int) 10;
                        break;
                    case "right":
                        x += (int) ((width) - g2d.getFontMetrics().getStringBounds(val, g2d).getWidth());
                        break;
                    default:
                        x += (int) 0;
                        break;
                }
            }

            g2d.drawString(val.trim(), x, y);
        });
    }

    public void drawText(Graphics g, String text, Integer l, Font font) {

        line = l;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(font);

        String[] words = text.split(" ");

        // Variabel untuk menyimpan teks setiap baris
        Map<Integer, String> texts = new HashMap<>();

        // Menggabungkan kata-kata menjadi baris-baris
        Integer nextLine = 0;
        for (String word : words) {

            String tempLine = texts.get(line) == null ? word : texts.get(line) + " " + word;
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(tempLine, g2d);
            if (bounds.getWidth() > width) {
                tempLine = word;
                line += (int) bounds.getHeight();
                texts.put(line, tempLine);
            } else {
                texts.put(line, tempLine);
            }
            nextLine = (int) bounds.getHeight();
        }
        line += nextLine;

        texts.entrySet().forEach((entry) -> {
            int x = 10;
            Integer y = entry.getKey();
            String val = entry.getValue();

            g2d.drawString(val.trim(), x, y);
        });
    }

    public void drawText(Graphics g, String text, Integer l) {

        line = l;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(font);

        String[] words = text.split(" ");

        // Variabel untuk menyimpan teks setiap baris
        Map<Integer, String> texts = new HashMap<>();

        // Menggabungkan kata-kata menjadi baris-baris
        Integer nextLine = 0;
        for (String word : words) {

            String tempLine = texts.get(line) == null ? word : texts.get(line) + " " + word;
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(tempLine, g2d);
            if (bounds.getWidth() > width) {
                tempLine = word;
                line += (int) bounds.getHeight();
                texts.put(line, tempLine);
            } else {
                texts.put(line, tempLine);
            }
            nextLine = (int) bounds.getHeight();
        }
        line += nextLine;
        texts.entrySet().forEach((entry) -> {
            int x = 10;
            Integer y = entry.getKey();
            String val = entry.getValue();

            g2d.drawString(val.trim(), x, y);
        });
    }

    public void drawText(Graphics g, String text) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(font);

        String[] words = text.split(" ");

        // Variabel untuk menyimpan teks setiap baris
        Map<Integer, String> texts = new HashMap<>();

        // Menggabungkan kata-kata menjadi baris-baris
        Integer nextLine = 0;
        for (String word : words) {

            String tempLine = texts.get(line) == null ? word : texts.get(line) + " " + word;
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(tempLine, g2d);
            if (bounds.getWidth() > width) {
                tempLine = word;
                line += (int) bounds.getHeight();
                texts.put(line, tempLine);
            } else {
                texts.put(line, tempLine);
            }
            nextLine = (int) bounds.getHeight();
        }
        line += nextLine;
        texts.entrySet().forEach((entry) -> {
            int x = 10;
            Integer y = entry.getKey();
            String val = entry.getValue();

            g2d.drawString(val.trim(), x, y);
        });
    }

    private List<String> splitTextIntoLines(String text, FontMetrics fontMetrics, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (fontMetrics.stringWidth(currentLine + " " + word) <= maxWidth) {
                currentLine.append(" ").append(word);
            } else {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString().trim());
        }

        return lines;
    }

    public void drawTextJustify(Graphics g, String text) {
        String inputText = "BARANG YANG SUDAH DITERIMA TIDAK DAPAT DITUKAR ATAU DIKEMBALIKAN, KECUALI SUDAH ADA KESEPAKATAN TERLEBIH DAHULU";
        int maxWidth = 200; // Maximum width in pixels

        Graphics2D g2d = (Graphics2D) g;

        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics(font);

        List<String> lines = splitTextIntoLines(inputText, fontMetrics, maxWidth);

        int lineHeight = fontMetrics.getHeight();
        int y = line; // Initial y-coordinate for drawing the text

        for (int o = 0; o < lines.size(); o++) {
            String line = lines.get(o);
            String[] words = line.split(" ");
            int numWords = words.length;
            int lineWidth = fontMetrics.stringWidth(line);
            int spaceWidth = 0;
            int extraSpace = 0;
            int cekData = lines.size() - o;
            if (cekData == 1) {
                spaceWidth = (195 - lineWidth) / (numWords - 1);
                extraSpace = (195 - lineWidth) % (numWords - 1);
            } else {
                spaceWidth = (maxWidth - lineWidth) / (numWords - 1);
                extraSpace = (maxWidth - lineWidth) % (numWords - 1);
            }

            int x = 10; // Initial x-coordinate for drawing the text

            for (int i = 0; i < numWords; i++) {
                g2d.drawString(words[i], x, y);

                if (cekData != 1) {
                    x += fontMetrics.stringWidth(words[i]) + spaceWidth;

                    if (extraSpace > 0) {
                        x += 1; // Add extra space
                        extraSpace--;
                    }
                } else {
                    x += fontMetrics.stringWidth(words[i]) + 10;
                }
            }

            y += lineHeight; // Move to the next line
        }
        line = y;
    }

    public void drawTextJustify2(Graphics g, String text) {
        String inputText = "MOHON CEK KEMBALI MOBIL ATAU BARANG ANDA, KAMI TIDAK MELAYANI KOMPLAIN SETELAH MENINGGALKAN TOKO KAMI, KECUALI ADA GARANSINYA";
        int maxWidth = 200; // Maximum width in pixels

        Graphics2D g2d = (Graphics2D) g;

        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics(font);

        List<String> lines = splitTextIntoLines(inputText, fontMetrics, maxWidth);

        int lineHeight = fontMetrics.getHeight();
        int y = line; // Initial y-coordinate for drawing the text

        for (int o = 0; o < lines.size(); o++) {
            String line = lines.get(o);
            String[] words = line.split(" ");
            int numWords = words.length;
            int lineWidth = fontMetrics.stringWidth(line);
            int spaceWidth = 0;
            int extraSpace = 0;
            int cekData = lines.size() - o;
            if (cekData == 1) {
                spaceWidth = (195 - lineWidth) / (numWords - 1);
                extraSpace = (195 - lineWidth) % (numWords - 1);
            } else {
                spaceWidth = (maxWidth - lineWidth) / (numWords - 1);
                extraSpace = (maxWidth - lineWidth) % (numWords - 1);
            }

            int x = 10; // Initial x-coordinate for drawing the text

            for (int i = 0; i < numWords; i++) {
                g2d.drawString(words[i], x, y);
                if (cekData != 1) {
                    x += fontMetrics.stringWidth(words[i]) + spaceWidth;

                    if (extraSpace > 0) {
                        x += 1; // Add extra space
                        extraSpace--;
                    }
                } else {
                    x += fontMetrics.stringWidth(words[i]) + 10;
                }
            }

            y += lineHeight; // Move to the next line
        }
        line = y;
    }

    public void dash(Graphics2D g2d) {
        // dash
        int startY = line;
        int lineStartX = 0;
        int lineEndX = (int) (width);
        int currentX = lineStartX;
        boolean drawDash = true;
        // Membuat garis putus-putus
        int dashLength = 5; // Panjang garis putus-putus
        int spaceLength = 2; // Panjang spasi antara garis putus-putus

        while (currentX < lineEndX) {
            if (drawDash) {
                g2d.drawLine(currentX, startY, currentX + dashLength, startY);
                currentX += dashLength;
            } else {
                currentX += spaceLength;
            }
            drawDash = !drawDash;
        }
        line += 10;
        // end dashv
    }

    public void drawLeftRight(Graphics g2d, String textLeft, String textRight, Integer l, Font font) {
        Integer currentLine = l;
        drawText(g2d, textLeft, currentLine, font);
        drawText(g2d, textRight, "right", currentLine, font);
        Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(textLeft, g2d);
        line = currentLine + (int) bounds.getHeight() - 5;
    }

    public void drawLeftCenterRight(Graphics g2d, String textLeft, String textCenter, String textRight, Integer l,
            Font font) {
        Integer currentLine = l;
        drawText(g2d, textLeft, currentLine, font);
        drawText(g2d, textCenter, "center", currentLine, font);
        drawText(g2d, textRight, "right", currentLine, font);
        Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(textLeft, g2d);
        line = currentLine + (int) bounds.getHeight() - 5;
    }

    public void drawTextY(Graphics g, String text, String align) {

        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(font);

        String[] words = text.split(" ");

        // Variabel untuk menyimpan teks setiap baris
        Map<Integer, String> texts = new HashMap<>();

        // Menggabungkan kata-kata menjadi baris-baris
        Integer nextLine = 0;
        for (String word : words) {

            String tempLine = texts.get(line) == null ? word : texts.get(line) + " " + word;
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(tempLine, g2d);
            if (bounds.getWidth() > width) {
                tempLine = word;
                // line += (int) bounds.getHeight();
                texts.put(line, tempLine);
            } else {
                texts.put(line, tempLine);
            }
            nextLine = (int) bounds.getHeight();
        }
        linePending = line + nextLine;

        texts.entrySet().forEach((entry) -> {
            int x = 0;
            Integer y = entry.getKey();
            String val = entry.getValue();
            if (null != align) {
                switch (align) {
                    case "center":
                        x = (int) ((width) / 2 - g2d.getFontMetrics().getStringBounds(val, g2d).getWidth() / 2);
                        break;
                    case "left":
                        x = (int) 10;
                        break;
                    case "right":
                        x = (int) ((width) - g2d.getFontMetrics().getStringBounds(val, g2d).getWidth());
                        break;
                    default:
                        x = (int) 0;
                        break;
                }
            }

            g2d.drawString(val.trim(), x, y);
        });
    }

    public void functionToExecute() {
        // Implement your desired function here
        this.getAntrian();
        System.out.println("Executing the function...");
    }

    public PrintThermal() {
        setTitle("Printer Selection");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);

        // Create a combo box to hold the printer names
        printerComboBox = new JComboBox<>();

        // Get the list of available printers
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

        // Populate the combo box with printer names
        for (PrintService printer : printServices) {
            printerComboBox.addItem(printer.getName());
        }

        // Create a text field
        textField = new JTextField();
        textField.setColumns(20);

        // Create a button to print
        JButton printButton = new JButton("Print");
        printButton.addActionListener(e -> {
            selectedPrinter = (String) printerComboBox.getSelectedItem();
            // this.getData();
            // this.NotaAll();
            String value = textField.getText();
            System.out.println("Selected printer: " + selectedPrinter + value);
            // TODO: Implement printing logic using the selected printer

            this.getAntrian();
            // Timer timer = new Timer();

            // TimerTask task = new TimerTask() {
            // @Override
            // public void run() {
            // // Call your function or perform the desired task here
            // functionToExecute();
            // }
            // };

            // Schedule the task to run every 5 seconds
            // timer.schedule(task, 0, 50000);
        });

        // Create a panel to hold the components
        JPanel panel = new JPanel();
        panel.add(printerComboBox);
        panel.add(textField);
        panel.add(printButton);

        // Add the panel to the frame
        add(panel);

        // Display the frame
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PrintThermal::new);
    }
}
