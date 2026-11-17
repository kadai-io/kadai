FROM icr.io/db2_community/db2:12.1.0.0

# Preserve the database settings required by KADAI's DB2 schema.
RUN sed -i "s/territory us collate using identity/territory en-us collate using 'CLDRCOLL_LDE_AS_CX_EX_FX_HX_NX_S3' PAGESIZE 32 K/" \
      /var/db2_setup/include/db2_common_functions \
    && grep -Fq "collate using 'CLDRCOLL_LDE_AS_CX_EX_FX_HX_NX_S3' PAGESIZE 32 K" \
      /var/db2_setup/include/db2_common_functions
