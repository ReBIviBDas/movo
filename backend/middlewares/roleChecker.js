/**
 * Middleware to check if user has required role
 * Usage: router.get('/admin-only', tokenChecker, roleChecker('admin'), handler)
 */
const roleChecker = (...allowedRoles) => {
    return (req, res, next) => {
        if (!req.loggedUser) {
            return res.status(401).json({
                type: 'authentication_error',
                title: 'Unauthorized',
                detail: 'Authentication required'
            });
        }

        const userRole = req.loggedUser.role;
        
        if (!allowedRoles.includes(userRole)) {
            return res.status(403).json({
                type: 'authorization_error',
                title: 'Forbidden',
                detail: `This action requires one of the following roles: ${allowedRoles.join(', ')}`
            });
        }

        next();
    };
};

module.exports = roleChecker;
