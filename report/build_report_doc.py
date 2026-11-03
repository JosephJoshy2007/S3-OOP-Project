import os
import re
import glob
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.enum.section import WD_SECTION
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import qn, nsdecls

def create_report():
    doc = docx.Document()

    # Configure Base Styles
    style_normal = doc.styles['Normal']
    style_normal.font.name = 'Times New Roman'
    style_normal.font.size = Pt(12)
    style_normal.font.color.rgb = RGBColor(0, 0, 0)
    style_normal.paragraph_format.line_spacing = 1.15
    style_normal.paragraph_format.space_after = Pt(4)
    style_normal.paragraph_format.space_before = Pt(0)

    # Configure document settings: borders do NOT surround headers and footers
    doc.settings.element.append(parse_xml(f'<w:bordersDoNotSurroundHeader {nsdecls("w")}/>'))
    doc.settings.element.append(parse_xml(f'<w:bordersDoNotSurroundFooter {nsdecls("w")}/>'))

    # Helper: Apply uniform section geometry with ample header/footer breathing room
    def apply_section_geometry(sec):
        sec.top_margin = Inches(1.15)
        sec.bottom_margin = Inches(1.15)
        sec.left_margin = Inches(1.0)
        sec.right_margin = Inches(1.0)
        sec.header_distance = Inches(0.45)
        sec.footer_distance = Inches(0.45)

    # Helper: Add page border (measured from text margin with 6pt offset)
    def add_page_border(section):
        sectPr = section._sectPr
        pgBorders = OxmlElement('w:pgBorders')
        pgBorders.set(qn('w:offsetFrom'), 'text')
        for b_name in ['top', 'left', 'bottom', 'right']:
            b = OxmlElement(f'w:{b_name}')
            b.set(qn('w:val'), 'single')
            b.set(qn('w:sz'), '12')   # 1.5 pt
            b.set(qn('w:space'), '6') # 6 pt from text margin (leaves ~35pt gap to header text)
            b.set(qn('w:color'), '000000')
            pgBorders.append(b)
        sectPr.append(pgBorders)

    # Configure Section 1
    s1 = doc.sections[0]
    apply_section_geometry(s1)
    add_page_border(s1)
    s1.different_first_page_header_footer = True

    # Roman numeral page numbering starting at 0 (Title page = 0 [hidden], Certificate = I, Declaration = II)
    pgNum1 = OxmlElement('w:pgNumType')
    pgNum1.set(qn('w:fmt'), 'upperRoman')
    pgNum1.set(qn('w:start'), '0')
    s1._sectPr.append(pgNum1)

    # Centered Roman numeral footer for pages 2 onwards
    f1 = s1.footer.paragraphs[0]
    f1.alignment = WD_ALIGN_PARAGRAPH.CENTER
    f1.paragraph_format.space_before = Pt(0)
    f1.paragraph_format.space_after = Pt(0)
    r1 = f1.add_run()
    r1.font.name = 'Times New Roman'
    r1.font.size = Pt(11)
    for tag in ['begin', 'separate', 'end']:
        c = OxmlElement('w:fldChar')
        c.set(qn('w:fldCharType'), tag)
        if tag == 'separate':
            t = OxmlElement('w:instrText')
            t.text = 'PAGE'
            r1._r.append(t)
        r1._r.append(c)

    # Helper: Set table borders to none
    def set_table_no_borders(table):
        tblPr = table._tbl.tblPr
        tblBorders = parse_xml(f'''
            <w:tblBorders {nsdecls("w")}>
                <w:top w:val="none" w:sz="0" w:space="0" w:color="auto"/>
                <w:bottom w:val="none" w:sz="0" w:space="0" w:color="auto"/>
                <w:left w:val="none" w:sz="0" w:space="0" w:color="auto"/>
                <w:right w:val="none" w:sz="0" w:space="0" w:color="auto"/>
                <w:insideH w:val="none" w:sz="0" w:space="0" w:color="auto"/>
                <w:insideV w:val="none" w:sz="0" w:space="0" w:color="auto"/>
            </w:tblBorders>
        ''')
        tblPr.append(tblBorders)

    # Helper: Set table cell margins to 0
    def set_cell_margins_zero(cell):
        tcPr = cell._tc.get_or_add_tcPr()
        tcMar = parse_xml(f'''
            <w:tcMar {nsdecls("w")}>
                <w:top w:w="0" w:type="dxa"/>
                <w:bottom w:w="0" w:type="dxa"/>
                <w:left w:w="0" w:type="dxa"/>
                <w:right w:w="0" w:type="dxa"/>
            </w:tcMar>
        ''')
        tcPr.append(tcMar)

    # Helper: Create Header Table (2 columns: left subject, right project name, sitting at top of header)
    def create_header_table(header_container):
        p0 = header_container.paragraphs[0]
        p0._element.getparent().remove(p0._element)

        tbl = header_container.add_table(rows=1, cols=2, width=Inches(6.5))
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        tbl.autofit = False
        set_table_no_borders(tbl)

        c_left, c_right = tbl.rows[0].cells[0], tbl.rows[0].cells[1]
        c_left.width = Inches(3.8)
        c_right.width = Inches(2.7)
        set_cell_margins_zero(c_left)
        set_cell_margins_zero(c_right)

        p_l = c_left.paragraphs[0]
        p_l.alignment = WD_ALIGN_PARAGRAPH.LEFT
        p_l.paragraph_format.space_before = Pt(0)
        p_l.paragraph_format.space_after = Pt(0)
        r_l = p_l.add_run("PBCST 304 Object Oriented Programming")
        r_l.font.name = 'Times New Roman'
        r_l.font.size = Pt(10)
        r_l.font.bold = True
        r_l.font.color.rgb = RGBColor(70, 70, 70)

        p_r = c_right.paragraphs[0]
        p_r.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        p_r.paragraph_format.space_before = Pt(0)
        p_r.paragraph_format.space_after = Pt(0)
        r_r = p_r.add_run("Movie Ticket Booking System")
        r_r.font.name = 'Times New Roman'
        r_r.font.size = Pt(10)
        r_r.font.bold = True
        r_r.font.color.rgb = RGBColor(70, 70, 70)

    # Helper: Create Footer Table (2 columns: left college, right Arabic page number)
    def create_footer_table(footer_container):
        p0 = footer_container.paragraphs[0]
        p0._element.getparent().remove(p0._element)

        tbl = footer_container.add_table(rows=1, cols=2, width=Inches(6.5))
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        tbl.autofit = False
        set_table_no_borders(tbl)

        c_left, c_right = tbl.rows[0].cells[0], tbl.rows[0].cells[1]
        c_left.width = Inches(4.5)
        c_right.width = Inches(2.0)
        set_cell_margins_zero(c_left)
        set_cell_margins_zero(c_right)

        p_l = c_left.paragraphs[0]
        p_l.alignment = WD_ALIGN_PARAGRAPH.LEFT
        p_l.paragraph_format.space_before = Pt(0)
        p_l.paragraph_format.space_after = Pt(0)
        r_l = p_l.add_run("Department of CSE, CE Kidangoor")
        r_l.font.name = 'Times New Roman'
        r_l.font.size = Pt(10)
        r_l.font.color.rgb = RGBColor(70, 70, 70)

        p_r = c_right.paragraphs[0]
        p_r.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        p_r.paragraph_format.space_before = Pt(0)
        p_r.paragraph_format.space_after = Pt(0)
        r_r = p_r.add_run()
        r_r.font.name = 'Times New Roman'
        r_r.font.size = Pt(10)
        r_r.font.bold = True
        r_r.font.color.rgb = RGBColor(0, 0, 0)
        for tag in ['begin', 'separate', 'end']:
            c = OxmlElement('w:fldChar')
            c.set(qn('w:fldCharType'), tag)
            if tag == 'separate':
                t = OxmlElement('w:instrText')
                t.text = 'PAGE'
                r_r._r.append(t)
            r_r._r.append(c)

    # Helper: Set table borders
    def set_table_borders(table, color="000000", sz="4", val="single"):
        tblPr = table._tbl.tblPr
        tblBorders = parse_xml(f'''
            <w:tblBorders {nsdecls("w")}>
                <w:top w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>
                <w:bottom w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>
                <w:left w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>
                <w:right w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>
                <w:insideH w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>
                <w:insideV w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>
            </w:tblBorders>
        ''')
        tblPr.append(tblBorders)

    # Helper: Set cell shading
    def set_cell_shading(cell, color_hex="F1F5F9"):
        shading = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{color_hex}"/>')
        cell._tc.get_or_add_tcPr().append(shading)

    # Helper: Set cell margins / padding
    def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
        tcPr = cell._tc.get_or_add_tcPr()
        tcMar = parse_xml(f'''
            <w:tcMar {nsdecls("w")}>
                <w:top w:w="{top}" w:type="dxa"/>
                <w:bottom w:w="{bottom}" w:type="dxa"/>
                <w:left w:w="{left}" w:type="dxa"/>
                <w:right w:w="{right}" w:type="dxa"/>
            </w:tcMar>
        ''')
        tcPr.append(tcMar)

    # ==========================================
    # PAGE 1: TITLE PAGE
    # ==========================================
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("COLLEGE OF ENGINEERING KIDANGOOR\n")
    r.font.bold = True
    r.font.size = Pt(16)
    r = p.add_run("KOTTAYAM - 686583\n")
    r.font.bold = True
    r.font.size = Pt(14)

    # College Emblem
    if os.path.exists("assets/report/college_logo.jpg"):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(4)
        p_img.paragraph_format.space_after = Pt(6)
        p_img.add_run().add_picture("assets/report/college_logo.jpg", width=Inches(2.2))

    p_sub = doc.add_paragraph()
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sub.paragraph_format.space_before = Pt(4)
    p_sub.paragraph_format.space_after = Pt(2)
    r = p_sub.add_run("MICROPROJECT REPORT ON\n")
    r.font.bold = True
    r.font.size = Pt(13)

    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(2)
    p_title.paragraph_format.space_after = Pt(6)
    r = p_title.add_run("MOVIE TICKET BOOKING SYSTEM")
    r.font.bold = True
    r.font.size = Pt(17)

    p_desc = doc.add_paragraph()
    p_desc.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_desc.paragraph_format.space_before = Pt(2)
    p_desc.paragraph_format.space_after = Pt(6)
    r = p_desc.add_run("Submitted to the APJ Abdul Kalam Technological University, Kerala for the partial fulfillment of the requirements for the award of the degree Bachelor of Technology in Computer Science and Engineering\n")
    r.font.italic = True
    r.font.size = Pt(11.5)

    p_by = doc.add_paragraph()
    p_by.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_by.paragraph_format.space_after = Pt(4)
    r = p_by.add_run("By")
    r.font.size = Pt(12)

    # Students Table
    tbl_students = doc.add_table(rows=6, cols=2)
    tbl_students.alignment = WD_TABLE_ALIGNMENT.CENTER
    tbl_students.autofit = False

    students_data = [
        ("JOSEPH JOSHY", "KGR25CS073"),
        ("KAAVYANANDA J NAIR", "KGR25CS076"),
        ("DEEKSHITHA JAYARAJ", "KGR25CS047"),
        ("AJIN SATHEESH", "KGR25CS019"),
        ("ADWAITH P MURALI", "KGR25CS014"),
        ("M A INDRAJITH MITHRA", "KGR25CS087")
    ]

    for idx, (s_name, s_reg) in enumerate(students_data):
        row = tbl_students.rows[idx]
        cell_l, cell_r = row.cells[0], row.cells[1]
        cell_l.width = Inches(3.2)
        cell_r.width = Inches(2.2)

        p_l = cell_l.paragraphs[0]
        p_l.paragraph_format.line_spacing = 1.05
        p_l.paragraph_format.space_after = Pt(1)
        r_l = p_l.add_run(s_name)
        r_l.font.bold = True
        r_l.font.size = Pt(11)

        p_r = cell_r.paragraphs[0]
        p_r.paragraph_format.line_spacing = 1.05
        p_r.paragraph_format.space_after = Pt(1)
        r_r = p_r.add_run(s_reg)
        r_r.font.bold = True
        r_r.font.size = Pt(11)

    p_guide = doc.add_paragraph()
    p_guide.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_guide.paragraph_format.space_before = Pt(8)
    p_guide.paragraph_format.space_after = Pt(2)
    r = p_guide.add_run("Guided By\n")
    r.font.italic = True
    r.font.size = Pt(12)
    r_g = p_guide.add_run("GARGI CHANDRABABU")
    r_g.font.bold = True
    r_g.font.size = Pt(13)

    p_ktu = doc.add_paragraph()
    p_ktu.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_ktu.paragraph_format.space_before = Pt(2)
    p_ktu.paragraph_format.space_after = Pt(4)
    r = p_ktu.add_run("APJ ABDUL KALAM TECHNOLOGICAL UNIVERSITY")
    r.font.bold = True
    r.font.size = Pt(13)

    if os.path.exists("assets/report/ktu_logo.jpg"):
        p_ktu_logo = doc.add_paragraph()
        p_ktu_logo.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_ktu_logo.paragraph_format.space_before = Pt(2)
        p_ktu_logo.paragraph_format.space_after = Pt(4)
        p_ktu_logo.add_run().add_picture("assets/report/ktu_logo.jpg", width=Inches(1.2))

    p_date = doc.add_paragraph()
    p_date.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_date.paragraph_format.space_before = Pt(2)
    p_date.paragraph_format.space_after = Pt(0)
    r = p_date.add_run("November 2026")
    r.font.size = Pt(12)

    doc.add_page_break()

    # ==========================================
    # PAGE 2: CERTIFICATE (Page I)
    # ==========================================
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("COLLEGE OF ENGINEERING KIDANGOOR\n")
    r.font.bold = True
    r.font.size = Pt(16)
    r = p.add_run("KOTTAYAM - 686583\n")
    r.font.bold = True
    r.font.size = Pt(14)

    if os.path.exists("assets/report/college_logo.jpg"):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(4)
        p_img.paragraph_format.space_after = Pt(6)
        p_img.add_run().add_picture("assets/report/college_logo.jpg", width=Inches(2.0))

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("DEPARTMENT OF COMPUTER SCIENCE AND ENGINEERING\n\n")
    r.font.bold = True
    r.font.size = Pt(13)
    r_c = p.add_run("CERTIFICATE")
    r_c.font.bold = True
    r_c.font.size = Pt(16)

    p_cert = doc.add_paragraph()
    p_cert.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_cert.paragraph_format.line_spacing = 1.3
    p_cert.paragraph_format.space_before = Pt(10)
    p_cert.paragraph_format.space_after = Pt(40)

    p_cert.add_run("This is to certify that the microproject work report entitled ‘")
    r_t = p_cert.add_run("MOVIE TICKET BOOKING SYSTEM")
    r_t.font.bold = True
    p_cert.add_run("’ submitted by ")

    students_cert = [
        ("JOSEPH JOSHY (KGR25CS073)", ", "),
        ("KAAVYANANDA J NAIR (KGR25CS076)", ", "),
        ("DEEKSHITHA JAYARAJ (KGR25CS047)", ", "),
        ("AJIN SATHEESH (KGR25CS019)", ", "),
        ("ADWAITH P MURALI (KGR25CS014)", " and "),
        ("M A INDRAJITH MITHRA (KGR25CS087)", " ")
    ]
    for s_entry, sep in students_cert:
        r_s = p_cert.add_run(s_entry)
        r_s.font.bold = True
        p_cert.add_run(sep)

    p_cert.add_run(
        "of Third Semester, B.Tech in Computer Science & Engineering, has been successfully carried out under our "
        "supervision and guidance in partial fulfillment of the requirements of the microproject for PBCST304 – Object "
        "Oriented Programming, prescribed by APJ Abdul Kalam Technological University, Kerala. It is further "
        "certified that this work is a bonafide record of the microproject carried out by the above students during "
        "the academic year 2026–2027."
    )

    # Signatures Table
    tbl_sig = doc.add_table(rows=1, cols=2)
    tbl_sig.alignment = WD_TABLE_ALIGNMENT.CENTER
    tbl_sig.autofit = False

    cell_g = tbl_sig.rows[0].cells[0]
    cell_h = tbl_sig.rows[0].cells[1]
    cell_g.width = Inches(3.2)
    cell_h.width = Inches(3.2)

    p_g = cell_g.paragraphs[0]
    p_g.paragraph_format.line_spacing = 1.15
    p_g.add_run("GUIDE\n\n\n\n").font.bold = True
    p_g.add_run("GARGI CHANDRABABU\n").font.bold = True
    p_g.add_run("Assistant Professor\nDept. of CSE\nCollege of Engineering Kidangoor")

    p_h = cell_h.paragraphs[0]
    p_h.paragraph_format.line_spacing = 1.15
    p_h.add_run("HOD\n\n\n\n").font.bold = True
    p_h.add_run("Dr. REKHA KS\n").font.bold = True
    p_h.add_run("Assistant Professor\nDept. of CSE\nCollege of Engineering Kidangoor")

    doc.add_page_break()

    # ==========================================
    # PAGE 3: DECLARATION (Page II)
    # ==========================================
    p_dec_title = doc.add_paragraph()
    p_dec_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_dec_title.paragraph_format.space_before = Pt(20)
    p_dec_title.paragraph_format.space_after = Pt(24)
    r = p_dec_title.add_run("DECLARATION")
    r.font.bold = True
    r.font.size = Pt(16)

    p_dec = doc.add_paragraph()
    p_dec.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_dec.paragraph_format.line_spacing = 1.25
    p_dec.paragraph_format.space_after = Pt(12)
    p_dec.add_run(
        "We hereby declare that the microproject report MOVIE TICKET BOOKING SYSTEM submitted for the partial "
        "fulfillment of the requirements of the Third Semester Microproject for the subject PBCST304 – Object "
        "Oriented Programming, prescribed by APJ Abdul Kalam Technological University, Kerala, is a bonafide work "
        "done by us under the guidance of Asst. Prof. GARGI CHANDRABABU."
    )

    p_dec2 = doc.add_paragraph()
    p_dec2.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_dec2.paragraph_format.line_spacing = 1.25
    p_dec2.paragraph_format.space_after = Pt(12)
    p_dec2.add_run(
        "The submission represents our ideas in our own words. Where ideas or words of others have been included, "
        "we have adequately and accurately cited and referred to the original sources."
    )

    p_dec3 = doc.add_paragraph()
    p_dec3.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_dec3.paragraph_format.line_spacing = 1.25
    p_dec3.paragraph_format.space_after = Pt(40)
    p_dec3.add_run(
        "We also declare that we have adhered to ethical and integrity standards and have not misinterpreted or "
        "fabricated any data, idea, fact, or source in our submission. We understand that any violation of the above "
        "will be a case for disciplinary action by the institution and/or the University. This report has not "
        "previously formed the basis for the award of any degree, diploma, or similar title from any other university."
    )

    tbl_dec = doc.add_table(rows=1, cols=2)
    tbl_dec.alignment = WD_TABLE_ALIGNMENT.CENTER
    tbl_dec.autofit = False

    c_dl = tbl_dec.rows[0].cells[0]
    c_dr = tbl_dec.rows[0].cells[1]
    c_dl.width = Inches(3.2)
    c_dr.width = Inches(3.2)

    p_dl = c_dl.paragraphs[0]
    p_dl.paragraph_format.line_spacing = 1.2
    p_dl.add_run("Place: Kidangoor\n").font.bold = True
    p_dl.add_run("Date: 3/11/2026").font.bold = True

    p_dr = c_dr.paragraphs[0]
    p_dr.paragraph_format.line_spacing = 1.15
    for s_name, _ in students_data:
        r = p_dr.add_run(f"{s_name}\n")
        r.font.bold = True

    doc.add_page_break()

    # ==========================================
    # PAGE 4: VISION AND MISSION OF COLLEGE (Page III)
    # ==========================================
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(80)
    p.paragraph_format.space_after = Pt(40)
    r = p.add_run("VISION AND MISSION OF COLLEGE")
    r.font.bold = True
    r.font.size = Pt(16)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(10)
    r = p.add_run("VISION")
    r.font.bold = True
    r.font.size = Pt(14)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(40)
    p.add_run("To be a leading engineering institution in the region, providing competent professionals,\nwho engage in lifelong learning, driven by social values.")

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(10)
    r = p.add_run("MISSION")
    r.font.bold = True
    r.font.size = Pt(14)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.add_run("To prepare engineering graduates for the development activities of the society and\nindustry, and to prepare them for higher engineering education.")

    doc.add_page_break()

    # ==========================================
    # PAGE 5: VISION AND MISSION OF DEPARTMENT (Page IV)
    # ==========================================
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(80)
    p.paragraph_format.space_after = Pt(40)
    r = p.add_run("VISION AND MISSION OF DEPARTMENT")
    r.font.bold = True
    r.font.size = Pt(16)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(10)
    r = p.add_run("VISION")
    r.font.bold = True
    r.font.size = Pt(14)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(40)
    p.add_run("To become a center of excellence in Computer Science and Engineering imparting quality\nprofessional education to develop competent professionals with social values who are capable\nof life long learning.")

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(10)
    r = p.add_run("MISSION")
    r.font.bold = True
    r.font.size = Pt(14)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.add_run("To impart quality technical education to students at undergraduate level through constant\nknowledge upgradation by maintaining pace with the latest sophisticated innovations , research\nand development and industry interaction in the field of Computer Science and Engineering\nwith focus on lifelong learning for the well-being of the society.")

    doc.add_page_break()

    # ==========================================
    # PAGE 6: PEO (Page V)
    # ==========================================
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(30)
    p.paragraph_format.space_after = Pt(30)
    r = p.add_run("PROGRAM EDUCATIONAL OBJECTIVES (PEO)")
    r.font.bold = True
    r.font.size = Pt(16)

    tbl_peo = doc.add_table(rows=5, cols=2)
    tbl_peo.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(tbl_peo)

    peo_items = [
        ("PEO1", "Have sound knowledge and technical skills required to remain productive in the field of computer science and engineering."),
        ("PEO2", "Be efficient team leaders, effective communicators and successful entrepreneurs."),
        ("PEO3", "Resolve technical problems with a positive outlook towards well-being of the society."),
        ("PEO4", "Function in diverse environments with the ability and competence to solve challenging problems."),
        ("PEO5", "Pursue lifelong learning and professional development through higher education.")
    ]

    for idx, (code, desc) in enumerate(peo_items):
        r_row = tbl_peo.rows[idx]
        c0, c1 = r_row.cells[0], r_row.cells[1]
        c0.width = Inches(1.3)
        c1.width = Inches(5.1)
        set_cell_margins(c0, 120, 120, 140, 140)
        set_cell_margins(c1, 120, 120, 140, 140)

        p0 = c0.paragraphs[0]
        p0.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r0 = p0.add_run(code)
        r0.font.bold = True

        p1 = c1.paragraphs[0]
        p1.alignment = WD_ALIGN_PARAGRAPH.LEFT
        p1.add_run(desc)

    doc.add_page_break()

    # ==========================================
    # PAGE 7 & 8: POs (Page VI & VII)
    # ==========================================
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(20)
    p.paragraph_format.space_after = Pt(20)
    r = p.add_run("PROGRAM OUTCOMES (POs)")
    r.font.bold = True
    r.font.size = Pt(16)

    po_items_1 = [
        ("PO 1", "Engineering knowledge: Apply the knowledge of mathematics, science, engineering fundamentals, and an engineering specialization to the solution of complex engineering problems."),
        ("PO 2", "Problem analysis: Identify, formulate, review research literature, and analyse complex engineering problems reaching substantiated conclusions using first principles of mathematics, natural sciences, and engineering sciences."),
        ("PO 3", "Design/development of solutions: Design solutions for complex engineering problems and design system components or processes that meet the specified needs with appropriate consideration for the public health and safety, and the cultural, societal, and environmental considerations."),
        ("PO 4", "Conduct investigations of complex problems: Use research-based knowledge and research methods including design of experiments, analysis and interpretation of data, and synthesis of the information to provide valid conclusions."),
        ("PO 5", "Modern tool usage: Create, select, and apply appropriate techniques, resources, and modern engineering and IT tools including prediction and modeling to complex engineering activities with an understanding of the limitations."),
        ("PO 6", "The engineer and society: Apply reasoning informed by the contextual knowledge to assess societal, health, safety, legal and cultural issues and the consequent responsibilities relevant to the professional engineering practice.")
    ]

    tbl_po1 = doc.add_table(rows=6, cols=2)
    tbl_po1.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(tbl_po1)

    for idx, (code, desc) in enumerate(po_items_1):
        r_row = tbl_po1.rows[idx]
        c0, c1 = r_row.cells[0], r_row.cells[1]
        c0.width = Inches(1.3)
        c1.width = Inches(5.1)
        set_cell_margins(c0, 100, 100, 140, 140)
        set_cell_margins(c1, 100, 100, 140, 140)

        p0 = c0.paragraphs[0]
        p0.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r0 = p0.add_run(code)
        r0.font.bold = True

        p1 = c1.paragraphs[0]
        p1.alignment = WD_ALIGN_PARAGRAPH.LEFT
        p1.add_run(desc)

    doc.add_page_break()

    # POs Part 2 (Page VII)
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(20)
    p.paragraph_format.space_after = Pt(20)

    po_items_2 = [
        ("PO 7", "Environment and sustainability: Understand the impact of the professional engineering solutions in societal and environmental contexts, and demonstrate the knowledge of, and need for sustainable development."),
        ("PO 8", "Ethics: Apply ethical principles and commit to professional ethics and responsibilities and norms of the engineering practice."),
        ("PO 9", "Individual and team work: Function effectively as an individual, and as a member or leader in diverse teams, and in multidisciplinary settings."),
        ("PO 10", "Communication: Communicate effectively on complex engineering activities with the engineering community and with society at large, such as, being able to comprehend and write effective reports and design documentation, make effective presentations, and give and receive clear instructions."),
        ("PO 11", "Project management and finance: Demonstrate knowledge and understanding of the engineering and management principles and apply these to one’s own work, as a member and leader in a team, to manage projects and in multidisciplinary environments."),
        ("PO 12", "Life-long learning: Recognize the need for, and have the preparation and ability to engage in independent and life-long learning in the broadest context of technological change.")
    ]

    tbl_po2 = doc.add_table(rows=6, cols=2)
    tbl_po2.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(tbl_po2)

    for idx, (code, desc) in enumerate(po_items_2):
        r_row = tbl_po2.rows[idx]
        c0, c1 = r_row.cells[0], r_row.cells[1]
        c0.width = Inches(1.3)
        c1.width = Inches(5.1)
        set_cell_margins(c0, 100, 100, 140, 140)
        set_cell_margins(c1, 100, 100, 140, 140)

        p0 = c0.paragraphs[0]
        p0.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r0 = p0.add_run(code)
        r0.font.bold = True

        p1 = c1.paragraphs[0]
        p1.alignment = WD_ALIGN_PARAGRAPH.LEFT
        p1.add_run(desc)

    doc.add_page_break()

    # ==========================================
    # PAGE 9: COs & CO-PO MAPPING (Page VIII)
    # ==========================================
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(20)
    p.paragraph_format.space_after = Pt(20)
    r = p.add_run("COURSE OUTCOMES (COs)")
    r.font.bold = True
    r.font.size = Pt(16)

    tbl_co = doc.add_table(rows=6, cols=2)
    tbl_co.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(tbl_co)

    # Header
    tbl_co.rows[0].cells[0].paragraphs[0].add_run("Code").font.bold = True
    tbl_co.rows[0].cells[0].paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
    tbl_co.rows[0].cells[1].paragraphs[0].add_run("Course Outcome").font.bold = True
    tbl_co.rows[0].cells[1].paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_cell_shading(tbl_co.rows[0].cells[0], "F1F5F9")
    set_cell_shading(tbl_co.rows[0].cells[1], "F1F5F9")

    co_data = [
        ("CO1", "Explain the process of writing, compiling, and executing basic Java programs, including their structure and components, to demonstrate proficiency."),
        ("CO2", "Utilize object-oriented programming principles in the design and implementation of Java applications."),
        ("CO3", "Develop and manage Java packages and interfaces, enhancing code modularity and reusability."),
        ("CO4", "Implement error handling using Java’s exception mechanisms and leverage interfaces for modular applications."),
        ("CO5", "Develop event-driven Java GUI applications with database connectivity using Swing and JDBC.")
    ]

    for idx, (code, desc) in enumerate(co_data):
        row = tbl_co.rows[idx + 1]
        c0, c1 = row.cells[0], row.cells[1]
        c0.width = Inches(1.3)
        c1.width = Inches(5.1)
        set_cell_margins(c0, 80, 80, 120, 120)
        set_cell_margins(c1, 80, 80, 120, 120)

        p0 = c0.paragraphs[0]
        p0.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p0.add_run(code).font.bold = True

        p1 = c1.paragraphs[0]
        p1.add_run(desc)

    p_map = doc.add_paragraph()
    p_map.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_map.paragraph_format.space_before = Pt(24)
    p_map.paragraph_format.space_after = Pt(14)
    r = p_map.add_run("CO-PO MAPPING")
    r.font.bold = True
    r.font.size = Pt(14)

    # 13 cols: CO, PO1 to PO12
    tbl_copo = doc.add_table(rows=6, cols=13)
    tbl_copo.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(tbl_copo)

    headers = ["", "PO1", "PO2", "PO3", "PO4", "PO5", "PO6", "PO7", "PO8", "PO9", "PO10", "PO11", "PO12"]
    for c_idx, h in enumerate(headers):
        cell = tbl_copo.rows[0].cells[c_idx]
        cell.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
        cell.paragraphs[0].add_run(h).font.bold = True
        set_cell_shading(cell, "F1F5F9")

    matrix = [
        ["CO1", "3", "3", "", "", "", "", "", "", "", "", "", "3"],
        ["CO2", "3", "3", "3", "", "", "", "", "", "", "", "", "3"],
        ["CO3", "3", "3", "3", "", "3", "", "", "", "", "", "", "3"],
        ["CO4", "3", "3", "3", "", "3", "", "", "", "", "", "", "3"],
        ["CO5", "3", "3", "3", "", "3", "", "", "", "", "", "", "3"]
    ]

    for r_idx, row_vals in enumerate(matrix):
        row = tbl_copo.rows[r_idx + 1]
        for c_idx, val in enumerate(row_vals):
            cell = row.cells[c_idx]
            cell.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
            r_val = cell.paragraphs[0].add_run(val)
            if c_idx == 0:
                r_val.font.bold = True

    doc.add_page_break()

    # ==========================================
    # PAGE 10: CONTENTS (Page IX)
    # ==========================================
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(20)
    p.paragraph_format.space_after = Pt(28)
    r = p.add_run("CONTENTS")
    r.font.bold = True
    r.font.size = Pt(16)

    # Table of contents table
    toc_items = [
        ("ABSTRACT", "1"),
        ("CHAPTER 1   INTRODUCTION", "2"),
        ("CHAPTER 2   AIM AND OBJECTIVES", "3"),
        ("CHAPTER 3   REQUIREMENT SPECIFICATION", "4"),
        ("CHAPTER 4   DATABASE DESIGN", "5–8"),
        ("CHAPTER 5   UML CLASS DIAGRAM", "9–11"),
        ("CHAPTER 6   SYSTEM OUTPUT / SCREENSHOTS", "12–16"),
        ("CHAPTER 7   CODE LISTING", "17–80"),
        ("CHAPTER 8   CONCLUSION", "81")
    ]

    tbl_toc = doc.add_table(rows=len(toc_items), cols=2)
    tbl_toc.alignment = WD_TABLE_ALIGNMENT.CENTER
    tbl_toc.autofit = False

    for idx, (title, pg) in enumerate(toc_items):
        row = tbl_toc.rows[idx]
        c0, c1 = row.cells[0], row.cells[1]
        c0.width = Inches(5.4)
        c1.width = Inches(1.0)
        set_cell_margins(c0, 100, 100, 60, 60)
        set_cell_margins(c1, 100, 100, 60, 60)

        p0 = c0.paragraphs[0]
        p0.paragraph_format.line_spacing = 1.3
        r0 = p0.add_run(title)
        r0.font.bold = True
        r0.font.size = Pt(11.5)

        p1 = c1.paragraphs[0]
        p1.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        p1.paragraph_format.line_spacing = 1.3
        r1 = p1.add_run(pg)
        r1.font.bold = True
        r1.font.size = Pt(11.5)

    # ==========================================
    # SECTION 2: ABSTRACT & MAIN BODY (PAGE 1 ONWARDS)
    # ==========================================
    s2 = doc.add_section(WD_SECTION.NEW_PAGE)
    apply_section_geometry(s2)
    add_page_border(s2)

    s2.header.is_linked_to_previous = False
    s2.footer.is_linked_to_previous = False
    s2.different_first_page_header_footer = False

    # Configure Section 2 Decimal Page Numbering (Starting at 1 for Abstract)
    pgNum2 = OxmlElement('w:pgNumType')
    pgNum2.set(qn('w:fmt'), 'decimal')
    pgNum2.set(qn('w:start'), '1')
    s2._sectPr.append(pgNum2)

    create_header_table(s2.header)
    create_footer_table(s2.footer)

    # ==========================================
    # ABSTRACT (Page 1)
    # ==========================================
    p_abs_title = doc.add_paragraph()
    p_abs_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_abs_title.paragraph_format.space_before = Pt(40)
    p_abs_title.paragraph_format.space_after = Pt(28)
    r = p_abs_title.add_run("ABSTRACT")
    r.font.bold = True
    r.font.size = Pt(16)

    p_abs1 = doc.add_paragraph()
    p_abs1.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_abs1.paragraph_format.line_spacing = 1.25
    p_abs1.paragraph_format.space_after = Pt(12)
    p_abs1.add_run(
        "The Movie Ticket Booking System (CineBook) is a comprehensive Java-based desktop software application "
        "engineered to automate, streamline, and modernise the end-to-end operations of theatrical multiplexes. "
        "Traditional cinema ticketing systems heavily suffer from manual box-office queues, delayed seat availability "
        "updates, duplicate reservations, and fragmented accounting ledgers. CineBook eliminates these inefficiencies by "
        "providing a fully integrated desktop platform developed strictly according to the core principles of Object-Oriented "
        "Programming (OOP) — namely abstraction, encapsulation, inheritance, and dynamic runtime polymorphism — paired "
        "with an event-driven graphical interface built using Java Swing and relational SQLite persistence managed via "
        "the Java Database Connectivity (JDBC) API."
    )

    p_abs2 = doc.add_paragraph()
    p_abs2.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_abs2.paragraph_format.line_spacing = 1.25
    p_abs2.paragraph_format.space_after = Pt(12)
    p_abs2.add_run(
        "The system enforces strict role-based access control (RBAC) separating Customers and Administrators. Customers "
        "can interactively browse the theatrical catalogue with high-resolution film posters, filter movies by title and genre, "
        "view screening schedules across multiple auditoriums, reserve tier-based seating through an interactive 2D graphical grid, "
        "execute simulated checkout via Strategy and Adapter payment gateways, and review past admissions with digital printable "
        "e-tickets. Administrators are granted exclusive access to an operations dashboard providing real-time theatrical catalogue "
        "management, automated showtime scheduling, financial transaction audits, and algorithmic recursive occupancy telemetry."
    )

    p_abs3 = doc.add_paragraph()
    p_abs3.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_abs3.paragraph_format.line_spacing = 1.25
    p_abs3.add_run(
        "Overall, the Movie Ticket Booking System demonstrates a complete practical implementation of the KTU S3 CSE Object-Oriented "
        "Programming (PBCST304) curriculum. It provides a modular, maintainable, and highly robust application architecture with "
        "custom exception hierarchies, double-checked locking Singleton patterns, and thread-safe relational persistence."
    )

    doc.add_page_break()

    # ==========================================
    # CHAPTER 1: INTRODUCTION (Page 2)
    # ==========================================
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(4)
    r = p.add_run("Chapter 1")
    r.font.bold = True
    r.font.size = Pt(18)

    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(18)
    r = p.add_run("INTRODUCTION")
    r.font.bold = True
    r.font.size = Pt(16)

    p_i1 = doc.add_paragraph()
    p_i1.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_i1.paragraph_format.line_spacing = 1.25
    p_i1.paragraph_format.space_after = Pt(12)
    p_i1.add_run(
        "In the contemporary entertainment industry, the operational efficiency of movie theatres and multiplexes hinges "
        "on prompt, automated, and secure customer self-service systems. Manual ticketing desks and paper ledger logs frequently "
        "suffer from severe bottlenecks, including inaccurate seat allocations, human scheduling errors, untracked financial transactions, "
        "and long customer waiting times. The Movie Ticket Booking System is designed to address these challenges by replacing manual "
        "procedures with an automated, reliable, and user-centric software solution. Built using pure standard Java SE and Swing, "
        "the application communicates seamlessly with an embedded SQLite database using standard JDBC drivers."
    )

    p_i2 = doc.add_paragraph()
    p_i2.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_i2.paragraph_format.line_spacing = 1.25
    p_i2.paragraph_format.space_after = Pt(12)
    p_i2.add_run(
        "The architecture of CineBook is rooted in Object-Oriented Programming (OOP) paradigms. User accounts inherit from a "
        "common abstract User entity, branching into Customer and AdminUser roles with dynamic polymorphic greetings and view routing. "
        "The system segregates interface contracts using interfaces like Cancellable and Searchable<T>, applies the Strategy design "
        "pattern for modular payment handling (Credit Card, UPI, Digital Wallet), and incorporates the structural Adapter pattern "
        "to interface legacy banking interfaces with modern checkout pipelines. To preserve data integrity across operational sessions, "
        "the system persists users, movies, screens, shows, bookings, transactions, and audit trails to SQLite using parameterized "
        "PreparedStatements that actively protect against SQL injection vulnerabilities."
    )

    p_i3 = doc.add_paragraph()
    p_i3.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_i3.paragraph_format.line_spacing = 1.25
    p_i3.add_run(
        "By employing a modular six-member team engineering division, the codebase cleanly decouples domain models, business service "
        "engines, GUI Swing views, and persistence layers. This project directly showcases the practical realization of Java syntax, "
        "GUI components, layout management, thread-safe memory state coordination, custom checked exception handling, and relational "
        "database connectivity."
    )

    doc.add_page_break()

    # ==========================================
    # CHAPTER 2: AIM AND OBJECTIVES (Page 3)
    # ==========================================
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(4)
    r = p.add_run("CHAPTER 2")
    r.font.bold = True
    r.font.size = Pt(18)

    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(18)
    r = p.add_run("AIM AND OBJECTIVES")
    r.font.bold = True
    r.font.size = Pt(16)

    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(6)
    r = p.add_run("AIM")
    r.font.bold = True
    r.font.size = Pt(14)

    p_aim = doc.add_paragraph()
    p_aim.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_aim.paragraph_format.line_spacing = 1.25
    p_aim.paragraph_format.space_after = Pt(20)
    p_aim.add_run(
        "The primary aim of the Movie Ticket Booking System is to design and develop a secure, robust, modular, and "
        "user-friendly Java desktop application that automates movie browsing, auditorium showtime scheduling, 2D interactive "
        "seat selection, payment simulation, ticket cancellation, and business reporting using Object-Oriented Programming "
        "principles and SQLite JDBC connectivity."
    )

    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(8)
    r = p.add_run("OBJECTIVES")
    r.font.bold = True
    r.font.size = Pt(14)

    objectives = [
        "To replace traditional manual counter ticketing with a computerized Java Swing desktop application.",
        "To apply foundational OOP principles including data abstraction, encapsulation, inheritance, polymorphism, and interfaces in a production-grade architecture.",
        "To develop an interactive graphical user interface using Swing components (JFrame, JPanel, CardLayout, GridLayout, JTable) and event-driven delegation listeners.",
        "To integrate an embedded SQLite database using JDBC for persistent storage of users, movies, screens, shows, bookings, financial transactions, and audit logs.",
        "To implement role-based authentication separating Customer and Administrator views, featuring a mandatory startup login dialog and session reset upon logout.",
        "To design an interactive 2D auditorium seat selection grid supporting dynamic tier-based pricing multipliers (Standard, Premium, VIP) and 5-minute checkout holds.",
        "To implement modular payment processing using the Strategy pattern (Card, UPI, Wallet) and Adapter pattern (Legacy Bank DirectDebit).",
        "To enforce business invariant safety guards, including non-cancellable past bookings, double-confirmation prevention, and safe movie deletion restrictions.",
        "To calculate real-time multiplex occupancy and revenue metrics using algorithmic recursion and structured audit logging."
    ]

    for obj in objectives:
        p_obj = doc.add_paragraph(style='List Bullet')
        p_obj.paragraph_format.line_spacing = 1.2
        p_obj.paragraph_format.space_after = Pt(6)
        p_obj.add_run(obj)

    doc.add_page_break()

    # ==========================================
    # CHAPTER 3: REQUIREMENT SPECIFICATION (Page 4)
    # ==========================================
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(4)
    r = p.add_run("CHAPTER 3")
    r.font.bold = True
    r.font.size = Pt(18)

    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(18)
    r = p.add_run("REQUIREMENT SPECIFICATION")
    r.font.bold = True
    r.font.size = Pt(16)

    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(6)
    r = p.add_run("3.1 SOFTWARE REQUIREMENTS")
    r.font.bold = True
    r.font.size = Pt(13)

    soft_reqs = [
        ("Operating System", "Microsoft Windows 10 / 11, Linux (Ubuntu / Fedora), or macOS"),
        ("Programming Language", "Java Standard Edition (Java SE 17+ / JDK 26)"),
        ("GUI Framework", "Java Swing & Abstract Window Toolkit (AWT)"),
        ("Database Engine", "SQLite 3 (Embedded Serverless Relational Database)"),
        ("Database Driver", "SQLite JDBC Driver (org.sqlite.JDBC v3.47.2.0)"),
        ("Compilation & Execution", "Standard JDK command-line tools (javac and java)"),
        ("Development Tools", "VS Code, IntelliJ IDEA, Eclipse, or NetBeans")
    ]

    for label, val in soft_reqs:
        p_r = doc.add_paragraph(style='List Bullet')
        p_r.paragraph_format.line_spacing = 1.15
        p_r.paragraph_format.space_after = Pt(4)
        p_r.add_run(f"{label}: ").font.bold = True
        p_r.add_run(val)

    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(16)
    p.paragraph_format.space_after = Pt(6)
    r = p.add_run("3.2 HARDWARE REQUIREMENTS")
    r.font.bold = True
    r.font.size = Pt(13)

    hard_reqs = [
        ("Processor", "Intel Core i3 / AMD Ryzen 3 or higher (minimum x64 architecture)"),
        ("Random Access Memory (RAM)", "Minimum 4 GB (Recommended: 8 GB or higher)"),
        ("Hard Disk Space", "Minimum 500 MB free disk space for project source, binaries, and SQLite database"),
        ("Display Resolution", "1280 × 800 resolution or higher (optimised for modern widescreen display)"),
        ("Input Peripherals", "Standard QWERTY Keyboard and Mouse / Touchpad")
    ]

    for label, val in hard_reqs:
        p_r = doc.add_paragraph(style='List Bullet')
        p_r.paragraph_format.line_spacing = 1.15
        p_r.paragraph_format.space_after = Pt(4)
        p_r.add_run(f"{label}: ").font.bold = True
        p_r.add_run(val)

    doc.add_page_break()

    # ==========================================
    # CHAPTER 4: DATABASE DESIGN (Pages 5–8)
    # ==========================================
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(4)
    r = p.add_run("CHAPTER 4")
    r.font.bold = True
    r.font.size = Pt(18)

    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(14)
    r = p.add_run("DATABASE DESIGN")
    r.font.bold = True
    r.font.size = Pt(16)

    p_db = doc.add_paragraph()
    p_db.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_db.paragraph_format.line_spacing = 1.25
    p_db.paragraph_format.space_after = Pt(14)
    p_db.add_run(
        "The Movie Ticket Booking System utilizes SQLite through the Java Database Connectivity (JDBC) API. "
        "The DatabaseManager singleton coordinates all database interactions, verifying the presence of the "
        "org.sqlite.JDBC driver, establishing a thread-safe connection to db/cinebook.db, and executing Data Definition "
        "Language (DDL) statements upon startup to instantiate the required schema. Foreign key constraints are actively "
        "enforced via SQLite PRAGMA directives to maintain referential integrity across movies, shows, and customer bookings. "
        "All queries and Data Manipulation Language (DML) operations utilize parameterized PreparedStatement instances, "
        "ensuring SQL injection prevention and efficient pre-compiled query plans."
    )

    # Tables list helper
    def render_schema_table(sec_num, sec_title, headers, rows_data):
        p_t = doc.add_paragraph()
        p_t.paragraph_format.space_before = Pt(10)
        p_t.paragraph_format.space_after = Pt(4)
        r = p_t.add_run(f"{sec_num} {sec_title}")
        r.font.bold = True
        r.font.size = Pt(12)

        tbl = doc.add_table(rows=len(rows_data) + 1, cols=len(headers))
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        tbl.autofit = False
        set_table_borders(tbl)

        for c_idx, h in enumerate(headers):
            c = tbl.rows[0].cells[c_idx]
            p = c.paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p.add_run(h).font.bold = True
            set_cell_shading(c, "F1F5F9")
            set_cell_margins(c, 60, 60, 100, 100)

        for r_idx, r_data in enumerate(rows_data):
            row = tbl.rows[r_idx + 1]
            for c_idx, val in enumerate(r_data):
                cell = row.cells[c_idx]
                p = cell.paragraphs[0]
                p.paragraph_format.line_spacing = 1.1
                p.paragraph_format.space_after = Pt(1)
                r_val = p.add_run(val)
                set_cell_margins(cell, 60, 60, 100, 100)
                if c_idx == 0:
                    r_val.font.bold = True

    # 4.1 USERS
    render_schema_table("4.1", "USERS TABLE", ["Column", "Data Type", "Constraint", "Description"], [
        ("id", "TEXT", "PRIMARY KEY", "Unique user account identifier (e.g. CUST-01, ADM-01)"),
        ("username", "TEXT", "UNIQUE, NOT NULL", "Login handle used for authentication (admin, user1, user2)"),
        ("password", "TEXT", "NOT NULL", "Account authentication password credential"),
        ("name", "TEXT", "NOT NULL", "Full legal name of the user profile"),
        ("email", "TEXT", "NOT NULL", "Contact email address for communications"),
        ("role", "TEXT", "NOT NULL", "Security role discriminator: 'Admin' or 'Customer'"),
        ("loyalty_points", "INTEGER", "DEFAULT 0", "Accumulated reward points for ticket purchases"),
        ("access_level", "TEXT", "OPTIONAL", "Permission scope for administrators (e.g. SuperAdmin)")
    ])

    # 4.2 MOVIES
    render_schema_table("4.2", "MOVIES TABLE", ["Column", "Data Type", "Constraint", "Description"], [
        ("id", "TEXT", "PRIMARY KEY", "Theatrical movie key (e.g. MOV-01)"),
        ("title", "TEXT", "NOT NULL", "Official feature film presentation title"),
        ("genre", "TEXT", "NOT NULL", "Film categorization (Sci-Fi, Action, Romance, etc.)"),
        ("duration", "INTEGER", "NOT NULL", "Total feature runtime in minutes"),
        ("language", "TEXT", "NOT NULL", "Audio language track of the screening"),
        ("certification", "TEXT", "NOT NULL", "Film board classification rating (PG, PG-13, R)"),
        ("description", "TEXT", "NOT NULL", "Official theatrical plot synopsis")
    ])

    doc.add_page_break()

    # 4.3 SCREENS
    render_schema_table("4.3", "SCREENS TABLE", ["Column", "Data Type", "Constraint", "Description"], [
        ("id", "TEXT", "PRIMARY KEY", "Auditorium screening hall identifier (e.g. SCR-01)"),
        ("name", "TEXT", "NOT NULL", "Commercial hall name (Screen 1 IMAX Laser, Dolby Atmos)"),
        ("seat_rows", "INTEGER", "NOT NULL", "Vertical seating row dimension (e.g. 5 rows: A-E)"),
        ("seat_cols", "INTEGER", "NOT NULL", "Horizontal seating column dimension (e.g. 8 cols: 1-8)")
    ])

    # 4.4 SHOWS
    render_schema_table("4.4", "SHOWS TABLE", ["Column", "Data Type", "Constraint", "Description"], [
        ("id", "TEXT", "PRIMARY KEY", "Screening showtime record key (e.g. SHW-01)"),
        ("movie_id", "TEXT", "FOREIGN KEY", "References movies(id)"),
        ("screen_id", "TEXT", "FOREIGN KEY", "References screens(id)"),
        ("show_time", "TEXT", "NOT NULL", "Formatted screening timestamp (yyyy-MM-dd HH:mm)"),
        ("base_price", "REAL", "NOT NULL", "Standard tier admission price in USD")
    ])

    # 4.5 BOOKINGS
    render_schema_table("4.5", "BOOKINGS TABLE", ["Column", "Data Type", "Constraint", "Description"], [
        ("id", "TEXT", "PRIMARY KEY", "Unique booking reservation reference (e.g. BKG-XXXXXXXX)"),
        ("customer_id", "TEXT", "FOREIGN KEY", "References users(id) of the purchasing customer"),
        ("show_id", "TEXT", "FOREIGN KEY", "References shows(id) of the scheduled screening"),
        ("seat_ids", "TEXT", "NOT NULL", "Comma-separated list of reserved seats (e.g. 'A1,A2')"),
        ("total_amount", "REAL", "NOT NULL", "Final payment total including 18% GST"),
        ("created_at", "TEXT", "NOT NULL", "Timestamp when reservation hold was initiated"),
        ("expires_at", "TEXT", "NOT NULL", "Timestamp when 5-minute checkout hold expires"),
        ("status", "TEXT", "NOT NULL", "Booking lifecycle: PENDING, CONFIRMED, CANCELLED"),
        ("cancellation_reason", "TEXT", "OPTIONAL", "Recorded justification for cancellation or refund")
    ])

    doc.add_page_break()

    # 4.6 TRANSACTIONS
    render_schema_table("4.6", "TRANSACTIONS TABLE", ["Column", "Data Type", "Constraint", "Description"], [
        ("id", "TEXT", "PRIMARY KEY", "Ledger transaction reference key (e.g. TXN-XXXXXXXX)"),
        ("booking_id", "TEXT", "FOREIGN KEY", "References bookings(id) associated with the payment"),
        ("payment_method", "TEXT", "NOT NULL", "Channel utilized: Credit Card, UPI, Wallet, Adapter"),
        ("amount", "REAL", "NOT NULL", "Financial transaction currency amount"),
        ("timestamp", "TEXT", "NOT NULL", "Execution timestamp of transaction authorization"),
        ("status", "TEXT", "NOT NULL", "Payment ledger status: SUCCESS, FAILED, REFUNDED"),
        ("reference", "TEXT", "NOT NULL", "Simulated gateway authorization or refund reference code")
    ])

    # 4.7 AUDIT_LOGS
    render_schema_table("4.7", "AUDIT_LOGS TABLE", ["Column", "Data Type", "Constraint", "Description"], [
        ("id", "INTEGER", "PRIMARY KEY AUTO", "Sequential event sequence identifier"),
        ("timestamp", "TEXT", "NOT NULL", "Timestamp of security or business operation"),
        ("action", "TEXT", "NOT NULL", "Detailed audit description of system event")
    ])

    p_note = doc.add_paragraph()
    p_note.paragraph_format.space_before = Pt(14)
    p_note.paragraph_format.space_after = Pt(14)
    p_note.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_note.add_run(
        "Figure 4.1 below illustrates the relational architecture and cardinalities uniting these entities. "
        "Customer accounts book shows, shows host independent seat grids linked to auditorium screens and movie titles, "
        "and confirmed bookings generate financial transactions within the persistent SQLite ledger."
    )

    doc.add_page_break()

    # FIGURE 4.1 ER DIAGRAM
    p_fig = doc.add_paragraph()
    p_fig.paragraph_format.space_before = Pt(10)
    p_fig.paragraph_format.space_after = Pt(8)
    p_fig.alignment = WD_ALIGN_PARAGRAPH.CENTER
    if os.path.exists("assets/report/fig4_1_er_diagram.png"):
        p_fig.add_run().add_picture("assets/report/fig4_1_er_diagram.png", width=Inches(6.4))
    p_cap = doc.add_paragraph()
    p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p_cap.add_run("Figure 4.1: Entity relationship diagram of CineBook database schema.")
    r.font.italic = True
    r.font.size = Pt(10.5)

    doc.add_page_break()

    # ==========================================
    # CHAPTER 5: UML CLASS DIAGRAM (Pages 9–11)
    # ==========================================
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(4)
    r = p.add_run("CHAPTER 5")
    r.font.bold = True
    r.font.size = Pt(18)

    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(14)
    r = p.add_run("UML CLASS DIAGRAM")
    r.font.bold = True
    r.font.size = Pt(16)

    p_u1 = doc.add_paragraph()
    p_u1.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_u1.paragraph_format.line_spacing = 1.25
    p_u1.paragraph_format.space_after = Pt(10)
    p_u1.add_run(
        "The UML class diagrams illustrate the structural organization, inheritance hierarchies, interfaces, and design "
        "patterns implemented across the CineBook application. The codebase separates domain state, service controllers, "
        "persistence mechanisms, and graphical Swing presentation."
    )

    p_u2 = doc.add_paragraph()
    p_u2.paragraph_format.space_after = Pt(4)
    p_u2.add_run("Core Model and Inheritance Hierarchies\n").font.bold = True
    p_u2.add_run(
        "The abstract User base class encapsulates shared security attributes (id, username, password, name, email, role) "
        "and enforces dynamic runtime polymorphism through the abstract getDashboardGreeting() method. Customer extends User "
        "with loyalty points tracking, while AdminUser extends User with administrative privileges. Booking implements the "
        "Cancellable interface, which mandates an isCancellable() state predicate (cancellable up to 2 hours before showtime) "
        "and a cancel(reason) mutator."
    )

    p_u3 = doc.add_paragraph()
    p_u3.paragraph_format.space_after = Pt(4)
    p_u3.add_run("Service, Strategy & Design Patterns\n").font.bold = True
    p_u3.add_run(
        "MovieService implements the generic Searchable<Movie> interface for query filtering. PaymentMethod serves as the abstract "
        "Strategy base, polymorphically dispatched across MockCardPayment, MockUpiPayment, and MockWalletPayment. The structural "
        "Adapter design pattern is implemented via LegacyBankGatewayAdapter, which adapts the incompatible LegacyBankGatewayApi "
        "to the system's PaymentGateway interface without modifying legacy code. BookingManager and DatabaseManager are implemented "
        "as thread-safe Singletons utilizing volatile instance variables and double-checked locking."
    )

    p_u4 = doc.add_paragraph()
    p_u4.paragraph_format.space_after = Pt(10)
    p_u4.add_run("Swing GUI Architecture\n").font.bold = True
    p_u4.add_run(
        "MainFrame extends JFrame and manages application routing through a Swing CardLayout. Content views (MoviePanel, "
        "MovieDetailsPanel, SeatSelectionPanel, CheckoutPanel, BookingConfirmationPanel, BookingHistoryPanel, and AdminPanel) "
        "all extend JPanel. LoginDialog extends JDialog to provide modal authentication on launch."
    )

    doc.add_page_break()

    # FIGURE 5.1 BACKEND UML
    p_fig = doc.add_paragraph()
    p_fig.paragraph_format.space_before = Pt(10)
    p_fig.paragraph_format.space_after = Pt(8)
    p_fig.alignment = WD_ALIGN_PARAGRAPH.CENTER
    if os.path.exists("assets/report/fig5_1_backend_uml.png"):
        p_fig.add_run().add_picture("assets/report/fig5_1_backend_uml.png", width=Inches(6.4))
    p_cap = doc.add_paragraph()
    p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p_cap.add_run("Figure 5.1: UML class diagram of domain model, service, and database classes.")
    r.font.italic = True
    r.font.size = Pt(10.5)

    doc.add_page_break()

    # FIGURE 5.2 SWING UML
    p_fig = doc.add_paragraph()
    p_fig.paragraph_format.space_before = Pt(10)
    p_fig.paragraph_format.space_after = Pt(8)
    p_fig.alignment = WD_ALIGN_PARAGRAPH.CENTER
    if os.path.exists("assets/report/fig5_2_swing_uml.png"):
        p_fig.add_run().add_picture("assets/report/fig5_2_swing_uml.png", width=Inches(6.4))
    p_cap = doc.add_paragraph()
    p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p_cap.add_run("Figure 5.2: UML class diagram of the Swing user interface classes.")
    r.font.italic = True
    r.font.size = Pt(10.5)

    doc.add_page_break()

    # ==========================================
    # CHAPTER 6: SYSTEM OUTPUT / SCREENSHOTS (Pages 12–16)
    # ==========================================
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(4)
    r = p.add_run("Chapter 6")
    r.font.bold = True
    r.font.size = Pt(18)

    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(14)
    r = p.add_run("SYSTEM OUTPUT / SCREENSHOTS")
    r.font.bold = True
    r.font.size = Pt(16)

    # Helper: Insert sized screenshot card
    def insert_screenshot(img_filename, caption_text, width_inches=4.7):
        img_path = os.path.join("screenshots", img_filename)
        if os.path.exists(img_path):
            p_img = doc.add_paragraph()
            p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p_img.paragraph_format.space_before = Pt(4)
            p_img.paragraph_format.space_after = Pt(2)
            p_img.add_run().add_picture(img_path, width=Inches(width_inches))

            p_c = doc.add_paragraph()
            p_c.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p_c.paragraph_format.space_before = Pt(2)
            p_c.paragraph_format.space_after = Pt(10)
            r = p_c.add_run(caption_text)
            r.font.italic = True
            r.font.size = Pt(9.5)

    # Page 12: Screenshots 6.1 and 6.2
    insert_screenshot("authentication.PNG", "Figure 6.1: Account authentication and startup login dialog with credential quick-fill chips.", 3.6)
    insert_screenshot("movie_catalog.PNG", "Figure 6.2: Theatrical movie catalogue browsing with live search, genre filter, and high-res posters.", 4.7)

    doc.add_page_break()

    # Page 13: Screenshots 6.3 and 6.4
    p_cont = doc.add_paragraph()
    p_cont.paragraph_format.space_after = Pt(6)
    r = p_cont.add_run("(CONTINUED)")
    r.font.bold = True
    r.font.size = Pt(14)

    insert_screenshot("movie_details.PNG", "Figure 6.3: Movie presentation details view with synopsis and scheduled auditorium showtime cards.", 4.7)
    insert_screenshot("seating.PNG", "Figure 6.4: Interactive auditorium seating grid featuring glowing cinema screen and tier multiplier badges.", 4.7)

    doc.add_page_break()

    # Page 14: Screenshots 6.5 and 6.6
    p_cont = doc.add_paragraph()
    p_cont.paragraph_format.space_after = Pt(6)
    r = p_cont.add_run("(CONTINUED)")
    r.font.bold = True
    r.font.size = Pt(14)

    insert_screenshot("checkout.PNG", "Figure 6.5: Checkout panel with 5-minute countdown hold timer and polymorphic payment simulation.", 4.7)
    insert_screenshot("confirmation.PNG", "Figure 6.6: Booking confirmation receipt verifying ticket generation and financial authorization.", 4.7)

    doc.add_page_break()

    # Page 15: Screenshots 6.7 and 6.8
    p_cont = doc.add_paragraph()
    p_cont.paragraph_format.space_after = Pt(6)
    r = p_cont.add_run("(CONTINUED)")
    r.font.bold = True
    r.font.size = Pt(14)

    insert_screenshot("mybookings.PNG", "Figure 6.7: Customer booking history ledger displaying status and cancellation eligibility.", 4.7)
    insert_screenshot("eticket.PNG", "Figure 6.8: Digital cinema e-ticket modal dialog with boarding pass formatting.", 3.8)

    doc.add_page_break()

    # Page 16: Screenshots 6.9 and 6.10
    p_cont = doc.add_paragraph()
    p_cont.paragraph_format.space_after = Pt(6)
    r = p_cont.add_run("(CONTINUED)")
    r.font.bold = True
    r.font.size = Pt(14)

    insert_screenshot("admin_console.PNG", "Figure 6.9: Administrator console displaying theatrical catalogue management and scheduling tabs.", 4.7)
    insert_screenshot("analytics.PNG", "Figure 6.10: Real-time business intelligence dashboard with recursive seat calculation telemetry.", 4.7)

    doc.add_page_break()

    # ==========================================
    # CHAPTER 7: CODE LISTING (Pages 17 onwards)
    # ==========================================
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(4)
    r = p.add_run("CHAPTER 7")
    r.font.bold = True
    r.font.size = Pt(18)

    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(14)
    r = p.add_run("CODE LISTING")
    r.font.bold = True
    r.font.size = Pt(16)

    p_code_intro = doc.add_paragraph()
    p_code_intro.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_code_intro.paragraph_format.line_spacing = 1.25
    p_code_intro.paragraph_format.space_after = Pt(10)
    p_code_intro.add_run(
        "Each Java source file of the CineBook project is listed below in full. To optimize readability and pagination "
        "for print binding, explanatory inline comments and redundant whitespace have been removed while retaining 100% "
        "of the operational logic, data types, method implementations, and architectural structure."
    )

    p_app_entry = doc.add_paragraph()
    p_app_entry.paragraph_format.space_after = Pt(4)
    p_app_entry.add_run("Application Entry Points & Test Suite\n").font.bold = True
    p_app_entry.add_run(
        "CineBookApp.java is the application entry point. It runs an automated 6-step regression verification suite before "
        "launching the graphical interface, verifying movie loading, independent seat instances, temporary reservation workflows, "
        "double-confirmation prevention, deletion guards, and recursive booked-seat algorithms. MainFrame.java serves as the "
        "primary application window coordinating view transitions via CardLayout."
    )

    p_oop_vis = doc.add_paragraph()
    p_oop_vis.paragraph_format.space_after = Pt(12)
    p_oop_vis.add_run("OOP Concepts Implemented in Code\n").font.bold = True
    p_oop_vis.add_run(
        "Inheritance: User -> Customer, AdminUser; PaymentMethod -> MockCardPayment, MockUpiPayment, MockWalletPayment. "
        "Abstraction: abstract User, abstract PaymentMethod. Interfaces: Cancellable, Searchable<T>, PaymentGateway. "
        "Design Patterns: Singleton (BookingManager, DatabaseManager), Strategy (PaymentMethod), Adapter (LegacyBankGatewayAdapter). "
        "Custom Exceptions: CineBookException hierarchy. Database: PreparedStatement and ResultSet CRUD operations via JDBC."
    )

    # Process all Java source files
    java_files = sorted(glob.glob("*.java"))

    def strip_code(content):
        # Remove block comments
        content = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)
        # Remove line comments
        content = re.sub(r'//.*', '', content)
        lines = [line.rstrip() for line in content.split('\n')]
        clean_lines = []
        prev_blank = False
        for line in lines:
            if line.strip():
                clean_lines.append(line)
                prev_blank = False
            elif not prev_blank:
                clean_lines.append('')
                prev_blank = True
        return '\n'.join(clean_lines).strip()

    for j_file in java_files:
        with open(j_file, 'r', encoding='utf-8') as f_in:
            raw_code = f_in.read()

        cleaned_code = strip_code(raw_code)

        # File Header Table (matching sample report's shaded header bar)
        tbl_file_hdr = doc.add_table(rows=1, cols=1)
        tbl_file_hdr.alignment = WD_TABLE_ALIGNMENT.CENTER
        tbl_file_hdr.autofit = False
        set_table_borders(tbl_file_hdr, color="94A3B8", sz="6")

        c_hdr = tbl_file_hdr.rows[0].cells[0]
        c_hdr.width = Inches(6.5)
        set_cell_shading(c_hdr, "F1F5F9")
        set_cell_margins(c_hdr, 40, 40, 80, 80)

        p_fh = c_hdr.paragraphs[0]
        p_fh.paragraph_format.line_spacing = 1.0
        p_fh.paragraph_format.space_after = Pt(0)
        r_fn = p_fh.add_run(f"File: {j_file}  |  Package: (default)")
        r_fn.font.name = 'Courier New'
        r_fn.font.size = Pt(8.5)
        r_fn.font.bold = True
        r_fn.font.color.rgb = RGBColor(15, 23, 42)

        # Code block
        p_code = doc.add_paragraph()
        p_code.paragraph_format.line_spacing = 1.0
        p_code.paragraph_format.space_before = Pt(2)
        p_code.paragraph_format.space_after = Pt(8)

        r_code = p_code.add_run(cleaned_code)
        r_code.font.name = 'Consolas'
        r_code.font.size = Pt(8.0)
        r_code.font.color.rgb = RGBColor(30, 41, 59)

    doc.add_page_break()

    # ==========================================
    # CHAPTER 8: CONCLUSION (Final Page)
    # ==========================================
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(4)
    r = p.add_run("Chapter 8")
    r.font.bold = True
    r.font.size = Pt(18)

    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(18)
    r = p.add_run("CONCLUSION")
    r.font.bold = True
    r.font.size = Pt(16)

    p_c1 = doc.add_paragraph()
    p_c1.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_c1.paragraph_format.line_spacing = 1.25
    p_c1.paragraph_format.space_after = Pt(14)
    p_c1.add_run(
        "The Movie Ticket Booking System (CineBook) successfully demonstrates the practical and production-grade application "
        "of Object-Oriented Programming (OOP) concepts in Java SE. By coupling a contemporary Java Swing graphical user "
        "interface with relational SQLite database connectivity through JDBC, the application provides an efficient, reliable, "
        "and automated platform for managing the operational lifecycle of a modern cinema theatre. The software drastically reduces "
        "manual paperwork, eliminates box-office ticketing queues, guarantees data persistence across sessions, and ensures strict "
        "referential integrity between shows, seat allocations, and payment records."
    )

    p_c2 = doc.add_paragraph()
    p_c2.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_c2.paragraph_format.line_spacing = 1.25
    p_c2.paragraph_format.space_after = Pt(14)
    p_c2.add_run(
        "The modular architecture of the codebase adheres strictly to industry software engineering best practices. The application "
        "implements clear separation of concerns, data encapsulation through private variables and accessors, class inheritance "
        "for user roles and payment channels, dynamic runtime polymorphism via method overriding, and interface segregation for "
        "search and cancellation capabilities. Advanced design patterns—including the double-checked locking Singleton pattern, "
        "Strategy payment pattern, and structural Adapter pattern—demonstrate the real-world utility of object-oriented design. "
        "Furthermore, robust custom exception handling protects the system from runtime state inconsistencies and duplicate bookings."
    )

    p_c3 = doc.add_paragraph()
    p_c3.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p_c3.paragraph_format.line_spacing = 1.25
    p_c3.add_run(
        "Future enhancements to the system could include integrating network socket broadcasting for multi-client concurrent bookings, "
        "SMS and email notification gateways for automated booking confirmations, QR-code scanner validation at theater entrance gates, "
        "and integration of external payment gateway APIs. Overall, the Movie Ticket Booking System effectively bridges theoretical "
        "concepts from the KTU PBCST304 syllabus with practical software craftsmanship, resulting in an extensible, reliable, and "
        "academically distinguished microproject."
    )

    # Save document
    output_filename = "CineBook_Microproject_Report.docx"
    doc.save(output_filename)
    print(f"Report successfully generated and saved to: {output_filename}")

    # Post-process with native Word engine to guarantee borders outside header/footer & refresh fields
    try:
        import win32com.client
        word = win32com.client.Dispatch("Word.Application")
        word.Visible = False
        full_path = os.path.abspath(output_filename)
        wb = word.Documents.Open(full_path)
        for sec in wb.Sections:
            sec.Borders.SurroundHeader = False
            sec.Borders.SurroundFooter = False
        wb.Fields.Update()
        wb.Save()
        wb.Close(False)
        word.Quit()
        print("Native Word formatting applied: headers/footers outside border and fields updated.")
    except Exception as ex:
        print(f"Native Word engine post-processing note: {ex}")

if __name__ == '__main__':
    create_report()
