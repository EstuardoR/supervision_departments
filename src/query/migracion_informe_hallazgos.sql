/*==============================================================================
  Migración: agrega la columna id_informe a Detalle_Resultados, usada por
  dao.clsInformeFinal (rama merge/ejecucion-to-master) para saber qué
  hallazgos quedaron incluidos en el informe final de cada supervisión.

  No hace falta una tabla nueva: cada hallazgo (Detalle_Resultados) solo
  puede pertenecer a un informe a la vez (cada supervisión tiene a lo sumo
  un Informe_Supervision), así que basta con una columna nullable aquí en
  vez de una tabla puente (Informe_Hallazgos).

  Ejecución sugerida: desde SSMS, sobre la base supervision_db ya creada.
==============================================================================*/
USE supervision_db;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.Detalle_Resultados')
      AND name = 'id_informe'
)
    BEGIN
        ALTER TABLE dbo.Detalle_Resultados
            ADD id_informe INT NULL
                CONSTRAINT FK_DetalleResultados_Informe
                REFERENCES dbo.Informe_Supervision (id_informe);
    END

GO
