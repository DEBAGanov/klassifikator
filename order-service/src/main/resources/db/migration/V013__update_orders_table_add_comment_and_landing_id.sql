-- Add missing columns to orders table

ALTER TABLE orders
ADD COLUMN IF NOT EXISTS landing_id BIGINT,
ADD COLUMN IF NOT EXISTS comment TEXT;

-- Update status default for new orders
ALTER TABLE orders 
ALTER COLUMN status SET DEFAULT 'PENDING';

-- Add comment
COMMENT ON COLUMN orders.landing_id IS 'Landing page ID from which the order was placed';
COMMENT ON COLUMN orders.comment IS 'Customer comment or special instructions';

