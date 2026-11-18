package vn.slmglobal.slmai.services.catalog;

import java.util.List;

/**
 * Trang HTML của ảnh báo giá gói tự tạo, dựng theo mẫu ảnh báo giá của thiết kế (rộng 1080px): đầu trang, tiêu đề và
 * số pha, thẻ thiết bị chính, giá trọn gói, danh mục thiết bị, chân trang. Kiểu chữ và màu ở quote-image/quote.css;
 * hệ hòa lưới khung đỏ, hệ Hy-Brid khung xanh như ảnh mẫu.
 */
final class QuoteImageTemplate {

    /** Tệp tĩnh trong classpath quote-image/ mà trang dùng */
    static final List<String> STATIC_FILES = List.of("quote.css", "BeVietnamPro-Regular.ttf",
            "BeVietnamPro-Medium.ttf", "BeVietnamPro-SemiBold.ttf", "BeVietnamPro-Bold.ttf",
            "BeVietnamPro-ExtraBold.ttf", "header-red.png", "footer-red.png", "header-green.png", "footer-green.png",
            "logo-solarmax.png");
    static final String SOLARMAX_LOGO = "logo-solarmax.png";

    /** Một dòng tiêu đề: phần thường và phần nổi màu (có thể rỗng) */
    record Title(String plain, String highlight) {
        int length() {
            return plain.length() + (highlight == null || highlight.isEmpty() ? 0 : 1 + highlight.length());
        }
    }

    record Spec(String label, String value) {
    }

    /** Bảo hành trên dải ruy băng: 12 NĂM, 18 THÁNG */
    record Warranty(int value, String unit) {
    }

    /**
     * Thẻ một thiết bị chính. Tên tệp ảnh / logo là tệp gửi kèm trang; null thì bỏ ảnh, logo thay bằng tên hãng.
     *
     * @param title      phần in hoa ("Tấm pin JA Solar")
     * @param titleLight phần nhạt sau dấu | ("20 kW"), có thể null
     * @param price      giá một đơn vị đã định dạng ("2.500.000"), null thì không có ô giá
     */
    record Device(String photo, Warranty warranty, String title, String titleLight, List<Spec> specs, String price,
                  String logo, String brand, String weight, String size) {
    }

    /** Một dòng danh mục thiết bị */
    record Row(String name, String logo, String brand, String quantity) {
    }

    record Page(boolean hybrid, List<Title> title, String production, Integer phases, List<Device> devices,
                String total, List<Row> rows) {
    }

    private QuoteImageTemplate() {
    }

    static String html(Page p) {
        String theme = p.hybrid() ? "green" : "red";
        StringBuilder h = new StringBuilder(8000);
        h.append("<!doctype html><html lang=\"vi\"><head><meta charset=\"utf-8\">")
                .append("<link rel=\"stylesheet\" href=\"quote.css\"></head><body class=\"theme-").append(theme)
                .append("\">\n");
        h.append("<div class=\"band\"><img src=\"header-").append(theme).append(".png\"></div>\n");

        int longest = p.title().stream().mapToInt(Title::length).max().orElse(0);
        String size = longest <= 19 ? "" : longest <= 23 ? " class=\"mid\"" : " class=\"long\"";
        h.append("<section class=\"hero\"><div class=\"text\"><h1").append(size).append(">");
        for (int i = 0; i < p.title().size(); i++) {
            Title t = p.title().get(i);
            if (i > 0) {
                h.append("<br>");
            }
            h.append(esc(t.plain()));
            if (t.highlight() != null && !t.highlight().isEmpty()) {
                h.append(" <span class=\"hi\">").append(esc(t.highlight())).append("</span>");
            }
        }
        h.append("</h1>");
        if (p.production() != null) {
            h.append("<div class=\"sub\">").append(esc(p.production())).append("</div>");
        }
        h.append("</div>");
        if (p.phases() != null) {
            h.append("<div class=\"phase\"><div class=\"n\">").append(p.phases())
                    .append("</div><div class=\"p\">PHA</div></div>");
        }
        h.append("</section>\n");

        if (!p.devices().isEmpty()) {
            h.append("<section class=\"devices\">\n");
            p.devices().forEach(d -> device(h, d));
            h.append("</section>\n");
        }

        h.append("<section class=\"total\"><div class=\"claim\">Trọn gói 100%<br>Cam kết không phát sinh<br>")
                .append("với mái ngói, mái tôn</div><div class=\"amount\">").append(esc(p.total()))
                .append("<sup>đ</sup></div></section>\n");

        if (!p.rows().isEmpty()) {
            h.append("<section class=\"list\"><h3>DANH MỤC THIẾT BỊ</h3><table>\n");
            for (int i = 0; i < p.rows().size(); i++) {
                Row r = p.rows().get(i);
                h.append("<tr><td class=\"no\">").append(i + 1).append("</td><td>").append(esc(r.name()))
                        .append("</td><td class=\"brand\">").append(logo(r.logo(), r.brand()))
                        .append("</td><td class=\"qty\">").append(esc(r.quantity())).append("</td></tr>\n");
            }
            h.append("</table></section>\n");
        }
        h.append("<div class=\"band\"><img src=\"footer-").append(theme).append(".png\"></div>\n</body></html>\n");
        return h.toString();
    }

    private static void device(StringBuilder h, Device d) {
        h.append("<div class=\"card\"><div class=\"photo\">");
        if (d.photo() != null) {
            h.append("<img src=\"").append(esc(d.photo())).append("\">");
        }
        if (d.warranty() != null) {
            h.append("<div class=\"ribbon\"><b>").append(d.warranty().value()).append("</b><span>")
                    .append(esc(d.warranty().unit())).append("</span><small>BẢO HÀNH VẬT LÝ</small></div>");
        }
        h.append("</div><div class=\"info\"><h2><span class=\"main\">").append(esc(d.title())).append("</span>");
        if (d.titleLight() != null) {
            h.append(" <span class=\"light\">| ").append(esc(d.titleLight())).append("</span>");
        }
        h.append("</h2>");
        for (Spec s : d.specs()) {
            h.append("<div class=\"spec\">").append(esc(s.label())).append(": <b>").append(esc(s.value()))
                    .append("</b></div>");
        }
        if (d.price() != null) {
            h.append("<div class=\"price-chip\">").append(esc(d.price())).append("<sup>đ</sup></div>");
        }
        h.append("</div><div class=\"facts\"><div class=\"box\"><div class=\"k\">Thương hiệu</div><div class=\"v\">")
                .append(logo(d.logo(), d.brand())).append("</div></div>");
        if (d.weight() != null || d.size() != null) {
            h.append("<div class=\"row\">");
            if (d.weight() != null) {
                h.append("<div class=\"box\"><div class=\"k\">Khối lượng</div><div class=\"v\"><span class=\"big\">")
                        .append(esc(d.weight())).append("</span><span class=\"unit\">KG</span></div></div>");
            }
            if (d.size() != null) {
                h.append("<div class=\"box\"><div class=\"k\">Kích thước</div><div class=\"v\"><span class=\"small\">")
                        .append(esc(d.size()).replace("\n", "<br>")).append("</span></div></div>");
            }
            h.append("</div>");
        }
        h.append("</div></div>\n");
    }

    private static String logo(String file, String brand) {
        if (file != null) {
            return "<img src=\"" + esc(file) + "\">";
        }
        return brand == null ? "" : "<span class=\"brand-text\">" + esc(brand) + "</span>";
    }

    static String esc(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder b = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            switch (c) {
                case '<' -> b.append("&lt;");
                case '>' -> b.append("&gt;");
                case '&' -> b.append("&amp;");
                case '"' -> b.append("&quot;");
                default -> b.append(c);
            }
        }
        return b.toString();
    }
}
