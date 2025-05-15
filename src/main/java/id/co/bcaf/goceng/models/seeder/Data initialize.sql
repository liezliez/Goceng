--hapus constraint
DECLARE @sql NVARCHAR(MAX) = '';
SELECT @sql += 'ALTER TABLE ' + QUOTENAME(OBJECT_NAME(parent_object_id)) +
               ' DROP CONSTRAINT ' + QUOTENAME(name) + '; '
FROM sys.foreign_keys;
EXEC sp_executesql @sql;

--drop tabel
DECLARE @sql NVARCHAR(MAX) = N'';
SELECT @sql += 'DROP TABLE ' + QUOTENAME(name) + ';' + CHAR(13)
FROM sys.tables;
EXEC sp_executesql @sql;




INSERT INTO roles (role_name) VALUES ('ROLE_SUPERADMIN');
INSERT INTO roles (role_name) VALUES ('ROLE_CUSTOMER');
INSERT INTO roles (role_name) VALUES ('ROLE_MARKETING');
INSERT INTO roles (role_name) VALUES ('ROLE_BRANCH_MANAGER');
INSERT INTO roles (role_name) VALUES ('ROLE_BACK_OFFICE');


INSERT INTO branch (id_branch, name, address, city, province, latitude, longitude)
VALUES
  ('42F47C49-01B9-423A-AA56-D160F8196641', 'Jakarta Pusat', 'Jl. Merdeka No.1', 'Jakarta', 'DKI Jakarta', -6.2088, 106.8456), -- Jakarta
  ('B43A94D7-4C5E-4F2D-8A7B-02477F36D65F', 'Bandung', 'Jl. Asia Afrika No.22', 'Bandung', 'Jawa Barat', -6.9175, 107.6191), -- Bandung
  ('31C93F27-70A9-44A0-A922-D0BC906A47A1', 'Surabaya', 'Jl. Pemuda No.17', 'Surabaya', 'Jawa Timur', -7.2504, 112.7688), -- Surabaya
  ('9B9A08D0-67C7-4783-B5F0-35E0E1A3D0D0', 'Medan', 'Jl. Gatot Subroto No.5', 'Medan', 'Sumatera Utara', 3.5952, 98.6722), -- Medan
  ('F4A8DA3E-0458-4694-B8F3-1D17E7B26937', 'Yogyakarta', 'Jl. Malioboro No.11', 'Yogyakarta', 'DI Yogyakarta', -7.7956, 110.3695), -- Yogyakarta
  ('84936B2B-960F-41A0-BE4B-B3C76D8A1705', 'Makassar', 'Jl. Sudirman No.9', 'Makassar', 'Sulawesi Selatan', -5.1477, 119.4328), -- Makassar
  ('A4C56E9D-07F2-4D6D-B6F1-5A4F6DC5975D', 'Denpasar', 'Jl. Teuku Umar No.6', 'Denpasar', 'Bali', -8.4095, 115.1889), -- Denpasar
  ('D699DB1D-F8E4-42B9-974E-F256E5F05339', 'Semarang', 'Jl. Pandanaran No.3', 'Semarang', 'Jawa Tengah', -6.9669, 110.4194), -- Semarang
  ('FD7B1328-60D9-44B3-9E93-0C2D126BC6EF', 'Balikpapan', 'Jl. Jendral Sudirman No.2', 'Balikpapan', 'Kalimantan Timur', -1.2654, 116.9026), -- Balikpapan
  ('B8828AE4-2266-4634-BB2A-4B8C870B32F5', 'Padang', 'Jl. Ahmad Yani No.7', 'Padang', 'Sumatera Barat', -0.9470, 100.4170); -- Padang

INSERT INTO users (id,account_status,email,name,password,id_branch,id_role) VALUES
	 (N'774EFFAA-8F6A-4CE4-8B0C-1671889E45E2',N'ACTIVE',N'customer1@example.net',N'Sadye_Rodriguez70',N'$2a$10$foJ.N4lCUqd3ZDqr/0c/AOgyaxqJp3IOPZshLdwRZ9gOZYptRGroC',N'B43A94D7-4C5E-4F2D-8A7B-02477F36D65F',2),
	 (N'200EEF2C-81AA-4DCA-A52A-183CD3F4E2EC',N'ACTIVE',N'superadmin@example.com',N'Johnathan Doe',N'$2a$10$jpR2pTEe6NrHYPNQ6Hytz.mhYO9noInMFbIuszGxapwbKRTW6MZdS',N'9B9A08D0-67C7-4783-B5F0-35E0E1A3D0D0',1),
	 (N'58DA2AC0-CC0B-4E40-BEA7-34B7C113DEB1',N'ACTIVE',N'marketing@example.net',N'Kellen_Pollich82',N'$2a$10$A7b59xPVw7M2UyjPReVnMOchwROJzqmWzrwdhlAQQz6ap4djiVDo2',N'B43A94D7-4C5E-4F2D-8A7B-02477F36D65F',3),
	 (N'43DEEE38-2CD2-4FBB-846E-400EA364F3A7',N'ACTIVE',N'branchmanager@example.net',N'Alysson15',N'$2a$10$RYLJDbegPx/kOsEUTJ3ofuQ0zluPv6MkVzZLEoKqswzEZ5.B9oz5m',N'B43A94D7-4C5E-4F2D-8A7B-02477F36D65F',4),
	 (N'CC46E765-5466-42EC-9A78-61DB78CBA56B',N'ACTIVE',N'customer2@example.org',N'Harrison_Runte',N'$2a$10$7Fn8e3226cbGyW9n5n5TqeTa9TlZ997qI8bgng8Lg3p6v31gKsmqu',N'B43A94D7-4C5E-4F2D-8A7B-02477F36D65F',2),
	 (N'783FA406-C5F8-47BE-A742-7E468A33EC28',N'ACTIVE',N'customer3@example.com',N'Terrill.Schowalter',N'$2a$10$WY.lsi7rEbVyl1TX1aNGU.gKaqySadsi5UeHLmC4qJhUEFl53PGsC',N'B43A94D7-4C5E-4F2D-8A7B-02477F36D65F',2),
	 (N'7D4A8E39-6E79-4A0C-BC23-B51DC6A0202B',N'ACTIVE',N'Jennie41@example.org',N'Emmanuelle67',N'$2a$10$3vTLhpTin5W9p77udMX0xuOq.UGxl0AXd6zANgfsVw3eAgCSLIXR6',N'B43A94D7-4C5E-4F2D-8A7B-02477F36D65F',2),
	 (N'FB2000D1-A44C-4D4C-8AEA-F949F01D5CE4',N'ACTIVE',N'backoffice@example.org',N'Domenic69',N'$2a$10$ye3FXnnwWuTCYkyXlWNvpuhjqKD23Xg0vtJkaRspDmNFNJ8JqPMaG',N'B43A94D7-4C5E-4F2D-8A7B-02477F36D65F',5);

INSERT INTO customers (id,account_no,address,credit_limit,date_of_birth,emergency_call,home_ownership_status,mother_maiden_name,nik,occupation,place_of_birth,salary,telp_no,id_user,name) VALUES
	 (N'D6B8AB01-A26D-4257-BD17-DEE00247FC81',N'64305084',N'5037 Farrell Stravenue',5000000.00,'1998-02-04',N'081298765432',N'RENT',N'Siti Aminah',N'47568238',N'Software Engineer',N'Redmond',5000000.00,N'62-345-363-0095',N'7D4A8E39-6E79-4A0C-BC23-B51DC6A0202B','Liezarda');

INSERT INTO employees (id,nip,name,version,work_status,id_branch,id_user) VALUES
	 (N'3B8DC861-1DAA-41F0-9D1D-12EA716DF984',N'NIP-94D047BD',N'Eugene_Hammes74',0,N'ACTIVE',N'42F47C49-01B9-423A-AA56-D160F8196641',N'200EEF2C-81AA-4DCA-A52A-183CD3F4E2EC'),
	 (N'DCFC898D-C1DC-460D-B9D0-3CBAB5B10C2D',N'NIP-E2D545E9',N'Kellen_Pollich82',0,N'ACTIVE',N'42F47C49-01B9-423A-AA56-D160F8196641',N'58DA2AC0-CC0B-4E40-BEA7-34B7C113DEB1'),
	 (N'31D86761-0931-4997-9F61-79A660B3669C',N'NIP-43242733',N'Domenic69',0,N'ACTIVE',N'42F47C49-01B9-423A-AA56-D160F8196641',N'FB2000D1-A44C-4D4C-8AEA-F949F01D5CE4'),
	 (N'F9A502A8-44B7-487C-970F-AA1C73011EA5',N'NIP-BFBC5F47',N'Alysson15',0,N'ACTIVE',N'42F47C49-01B9-423A-AA56-D160F8196641',N'43DEEE38-2CD2-4FBB-846E-400EA364F3A7');

-- Inserting features with both 'feature_name' and 'route'
INSERT INTO features (feature_name)
VALUES
    ('VIEW_USER'),
    ('VIEW_ALL_USERS'),
    ('VIEW_USER_BY_ID'),
    ('VIEW_USERS_BY_STATUS'),
    ('CREATE_USER'),
    ('UPDATE_USER'),
    ('DELETE_USER'),
    ('RESTORE_USER'),
    ('EDIT_USER');

-- Assign features to roles (for example)
INSERT INTO role_features (id_role, id_feature)
VALUES
    -- Superadmin has all features
    (1, 1), -- Superadmin -> VIEW_USER
    (1, 2), -- Superadmin -> VIEW_ALL_USERS
    (1, 3), -- Superadmin -> VIEW_USER_BY_ID
    (1, 4), -- Superadmin -> VIEW_USERS_BY_STATUS
    (1, 5), -- Superadmin -> CREATE_USER
    (1, 6), -- Superadmin -> UPDATE_USER
    (1, 7), -- Superadmin -> DELETE_USER
    (1, 8), -- Superadmin -> RESTORE_USER

    -- Marketing role
    (2, 1), -- Marketing -> VIEW_USER
    (2, 2), -- Marketing -> VIEW_ALL_USERS
    (2, 3), -- Marketing -> VIEW_USER_BY_ID
    (2, 4), -- Marketing -> VIEW_USERS_BY_STATUS
    (2, 5), -- Marketing -> CREATE_USER

    -- Branch Manager role
    (3, 1), -- Branch Manager -> VIEW_USER
    (3, 2), -- Branch Manager -> VIEW_ALL_USERS
    (3, 3), -- Branch Manager -> VIEW_USER_BY_ID
    (3, 4), -- Branch Manager -> VIEW_USERS_BY_STATUS
    (3, 5), -- Branch Manager -> CREATE_USER
    (3, 6), -- Branch Manager -> UPDATE_USER
    (3, 7), -- Branch Manager -> DELETE_USER

    -- Back Office role
    (4, 1), -- Back Office -> VIEW_USER
    (4, 2), -- Back Office -> VIEW_ALL_USERS
    (4, 3), -- Back Office -> VIEW_USER_BY_ID
    (4, 4), -- Back Office -> VIEW_USERS_BY_STATUS
    (4, 5), -- Back Office -> CREATE_USER
    (4, 6), -- Back Office -> UPDATE_USER
    (4, 7); -- Back Office -> DELETE_USER
